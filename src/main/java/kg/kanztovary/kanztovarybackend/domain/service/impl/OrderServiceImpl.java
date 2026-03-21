package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.*;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.OrderRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.OrderStatusHistoryRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.ProductRepository;
import kg.kanztovary.kanztovarybackend.domain.dto.order.*;
import kg.kanztovary.kanztovarybackend.domain.dto.orderitem.OrderItemDto;
import kg.kanztovary.kanztovarybackend.domain.dto.orderitem.OrderItemResponse;
import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import kg.kanztovary.kanztovarybackend.domain.enums.OrderStatus;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.mapper.ProductMapper;
import kg.kanztovary.kanztovarybackend.domain.service.OrderService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ProductMapper productMapper;

    /** Стоимость доставки  */
    @Value("${order.delivery-fee}")
    private BigDecimal deliveryFee;

    /** Порог бесплатной доставки */
    @Value("${order.free-delivery-threshold}")
    private BigDecimal freeDeliveryThreshold;


    @Override
    public OrderResponse create(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseException(ResponseStatus.BAD_REQUEST.getCode(),
                    "Список товаров не может быть пустым");
        }
        if (request.getDeliveryType() == null) {
            throw new ResponseException(ResponseStatus.BAD_REQUEST.getCode(),
                    "Укажите способ получения заказа");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new ResponseException(ResponseStatus.BAD_REQUEST.getCode(),
                    "Укажите номер телефона для связи");
        }
        if (request.getDeliveryType() == DeliveryType.DELIVERY
                && (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank())) {
            throw new ResponseException(ResponseStatus.BAD_REQUEST.getCode(),
                    "При выборе доставки укажите адрес");
        }

        User currentUser = currentUser();
        log.info("Создание заказа для пользователя: {}", currentUser.getEmail());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // Временный order для builder
        Order order = Order.builder()
                .user(currentUser)
                .status(OrderStatus.NEW)
                .deliveryType(request.getDeliveryType())
                .deliveryAddress(request.getDeliveryType() == DeliveryType.DELIVERY
                        ? request.getDeliveryAddress()
                        : null)
                .phoneNumber(request.getPhoneNumber())
                .totalPrice(BigDecimal.ZERO)
                .deliveryFee(BigDecimal.ZERO)
                .build();

        for (OrderItemDto itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResponseException(
                            ResponseStatus.PRODUCT_NOT_FOUND.getCode(),
                            ResponseStatus.PRODUCT_NOT_FOUND.getMessage()
                    ));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new ResponseException(ResponseStatus.NO_STOCK_SPACE.getCode(),
                        ResponseStatus.NO_STOCK_SPACE.getMessage() + ": " + product.getName());
            }

            BigDecimal unitPrice = product.getDiscountPrice() != null
                    ? product.getDiscountPrice()
                    : product.getPrice();

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            items.add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(lineTotal)
                    .build());

            subtotal = subtotal.add(lineTotal);
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
        }

        BigDecimal calculatedDeliveryFee = calculateDeliveryFee(request.getDeliveryType(), subtotal);

        order.setItems(items);
        order.setTotalPrice(subtotal);
        order.setDeliveryFee(calculatedDeliveryFee);
        orderRepository.save(order);

        saveHistory(order, null, OrderStatus.NEW, currentUser.getEmail());

        log.info("Заказ #{} создан: способ={}, сумма товаров={}, доставка={}, итого={}",
                order.getId(), request.getDeliveryType(), subtotal,
                calculatedDeliveryFee, order.getGrandTotal());

        return mapToResponse(order);
    }


    @Override
    @Transactional(readOnly = true)
    public OrderPageResponse getMyOrders(OrderFilterRequest filter) {
        User user = currentUser();
        Page<Order> page = orderRepository.findByUser(
                user.getId(),
                filter.getStatus(),
                toStartOfDay(filter.getFrom()),
                toEndOfDay(filter.getTo()),
                PageRequest.of(filter.getPage(), filter.getSize())
        );
        log.info("История заказов пользователя {}: всего {}", user.getEmail(), page.getTotalElements());
        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(Long id) {
        User user = currentUser();
        Order order = orderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.ORDER_NOT_FOUND.getCode(),
                        ResponseStatus.ORDER_NOT_FOUND.getMessage()
                ));
        return mapToResponse(order);
    }


    @Override
    @Transactional(readOnly = true)
    public OrderPageResponse getAllOrders(OrderFilterRequest filter) {
        Page<Order> page = orderRepository.findAllWithFilters(
                filter.getStatus(),
                filter.getDeliveryType(),
                filter.getUserId(),
                toStartOfDay(filter.getFrom()),
                toEndOfDay(filter.getTo()),
                PageRequest.of(filter.getPage(), filter.getSize())
        );
        log.info("Admin: список заказов, всего {}", page.getTotalElements());
        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.ORDER_NOT_FOUND.getCode(),
                        ResponseStatus.ORDER_NOT_FOUND.getMessage()
                ));
        return mapToResponse(order);
    }

    @Override
    public OrderResponse updateStatus(Long id, UpdateOrderRequest request) {
        Order order = orderRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.ORDER_NOT_FOUND.getCode(),
                        ResponseStatus.ORDER_NOT_FOUND.getMessage()
                ));

        OrderStatus previous = order.getStatus();
        OrderStatus next = request.getStatus();

        if (previous == next) {
            return mapToResponse(order);
        }

        order.setStatus(next);
        orderRepository.save(order);

        saveHistory(order, previous, next, currentUser().getEmail());
        log.info("Статус заказа #{} изменён: {} → {}", id, previous, next);

        return mapToResponse(order);
    }

    @Override
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseException(
                    ResponseStatus.ORDER_NOT_FOUND.getCode(),
                    ResponseStatus.ORDER_NOT_FOUND.getMessage()
            );
        }
        orderRepository.deleteById(id);
        log.info("Заказ #{} удалён", id);
    }


    /**
     * Рассчитывает стоимость доставки по бизнес-правилам:
     * <ul>
     *   <li>PICKUP → 0 (клиент забирает сам)</li>
     *   <li>DELIVERY, сумма > порога → 0 (бесплатная доставка)</li>
     *   <li>DELIVERY, сумма ≤ порога → фиксированная стоимость из конфига</li>
     * </ul>
     */
    private BigDecimal calculateDeliveryFee(DeliveryType deliveryType, BigDecimal subtotal) {
        if (deliveryType == DeliveryType.PICKUP) {
            return BigDecimal.ZERO;
        }
        // DELIVERY: бесплатно при заказе свыше порога
        return subtotal.compareTo(freeDeliveryThreshold) > 0
                ? BigDecimal.ZERO
                : deliveryFee;
    }


    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void saveHistory(Order order, OrderStatus previous, OrderStatus next, String changedBy) {
        historyRepository.save(OrderStatusHistory.builder()
                .order(order)
                .previousStatus(previous)
                .newStatus(next)
                .changedBy(changedBy)
                .build());
    }

    private OrderPageResponse toPageResponse(Page<Order> page) {
        return OrderPageResponse.builder()
                .orders(page.getContent().stream().map(this::mapToResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productName(item.getProduct().getName())
                        .product(productMapper.toResponse(item.getProduct()))
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        List<OrderStatusHistoryDto> history = order.getStatusHistory() != null
                ? order.getStatusHistory().stream()
                        .map(h -> OrderStatusHistoryDto.builder()
                                .previousStatus(h.getPreviousStatus() != null ? h.getPreviousStatus().name() : null)
                                .newStatus(h.getNewStatus().name())
                                .changedBy(h.getChangedBy())
                                .changedAt(h.getChangedAt())
                                .build())
                        .toList()
                : List.of();

        BigDecimal fee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;

        return OrderResponse.builder()
                .id(order.getId())
                .username(order.getUser() != null ? order.getUser().getUsername() : null)
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .deliveryFee(fee)
                .grandTotal(order.getTotalPrice().add(fee))
                .deliveryType(order.getDeliveryType() != null ? order.getDeliveryType().name() : null)
                .deliveryAddress(order.getDeliveryAddress())
                .phoneNumber(order.getPhoneNumber())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .statusHistory(history)
                .build();
    }

    private LocalDateTime toStartOfDay(java.time.LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime toEndOfDay(java.time.LocalDate date) {
        return date != null ? date.atTime(23, 59, 59) : null;
    }
}