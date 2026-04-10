package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.*;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.CartItemRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.CartRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.ProductRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.ProductVariantRepository;
import kg.kanztovary.kanztovarybackend.domain.dto.cart.*;
import kg.kanztovary.kanztovarybackend.domain.dto.order.CreateOrderRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.order.OrderResponse;
import kg.kanztovary.kanztovarybackend.domain.dto.orderitem.OrderItemDto;
import kg.kanztovary.kanztovarybackend.domain.enums.CartStatus;
import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import kg.kanztovary.kanztovarybackend.domain.enums.ProductStatus;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.service.CartService;
import kg.kanztovary.kanztovarybackend.domain.service.OrderService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderService orderService;


    @Override
    @Transactional(readOnly = true)
    public CartDto getCart() {
        User user = currentUser();
        Cart cart = findOrCreateCart(user);
        return toDto(cart);
    }


    @Override
    public CartDto addItem(AddToCartRequest request) {
        User user = currentUser();
        Cart cart = findOrCreateCart(user);

        Product product = findActiveProduct(request.getProductId());
        ProductVariant variant = resolveVariant(request.getVariantId(), product);

        long availableStock = variant != null
                ? variant.getStockQuantity()
                : product.getStockQuantity();

        Optional<CartItem> existing = variant != null
                ? cartItemRepository.findByCartIdAndProductIdAndVariantId(cart.getId(), product.getId(), variant.getId())
                : cartItemRepository.findByCartIdAndProductIdAndVariantIsNull(cart.getId(), product.getId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            validateStock(availableStock, newQty, product.getName());
            item.setQuantity(newQty);
            log.info("Увеличено количество позиции id={} до {}", item.getId(), newQty);
        } else {
            validateStock(availableStock, request.getQuantity(), product.getName());
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(item);
            log.info("Товар '{}' добавлен в корзину пользователя {}", product.getName(), user.getEmail());
        }

        cartRepository.save(cart);
        return toDto(cart);
    }


    @Override
    public CartDto updateItem(Long itemId, UpdateCartItemRequest request) {
        CartItem item = findCartItem(itemId);

        long availableStock = item.getVariant() != null
                ? item.getVariant().getStockQuantity()
                : item.getProduct().getStockQuantity();

        validateStock(availableStock, request.getQuantity(), item.getProduct().getName());

        item.setQuantity(request.getQuantity());
        log.info("Количество позиции id={} изменено на {}", itemId, request.getQuantity());

        cartRepository.save(item.getCart());
        return toDto(item.getCart());
    }


    @Override
    public CartDto removeItem(Long itemId) {
        CartItem item = findCartItem(itemId);
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        log.info("Позиция id={} удалена из корзины", itemId);
        return toDto(cart);
    }


    @Override
    public void clearCart() {
        User user = currentUser();
        Cart cart = findOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Корзина пользователя {} очищена", user.getEmail());
    }


    @Override
    public OrderResponse checkout(CheckoutRequest request) {
        User user = currentUser();
        Cart cart = findOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            throw new ResponseException(
                    ResponseStatus.CART_IS_EMPTY.getCode(),
                    ResponseStatus.CART_IS_EMPTY.getMessage()
            );
        }

        List<OrderItemDto> orderItems = cart.getItems().stream()
                .map(item -> {
                    OrderItemDto dto = new OrderItemDto();
                    dto.setProductId(item.getProduct().getId());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                })
                .toList();

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setItems(orderItems);
        orderRequest.setDeliveryType(request.getDeliveryType());
        orderRequest.setDeliveryAddress(request.getDeliveryType() == DeliveryType.DELIVERY
                ? request.getDeliveryAddress()
                : null);
        orderRequest.setPhoneNumber(request.getPhoneNumber());

        OrderResponse order = orderService.create(orderRequest);
        log.info("Заказ #{} оформлен из корзины пользователя {} ({})",
                order.getId(), user.getEmail(), request.getDeliveryType());

        cart.setStatus(CartStatus.ORDERED);
        cartRepository.save(cart);

        return order;
    }


    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /** Находит активную корзину пользователя или создаёт новую */
    private Cart findOrCreateCart(User user) {
        return cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .status(CartStatus.ACTIVE)
                            .build();
                    log.info("Создана новая корзина для пользователя {}", user.getEmail());
                    return cartRepository.save(newCart);
                });
    }

    private Product findActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.PRODUCT_NOT_FOUND.getCode(),
                        ResponseStatus.PRODUCT_NOT_FOUND.getMessage()
                ));
        if (product.getStatus() == ProductStatus.DELETED || product.getStatus() == ProductStatus.INACTIVE) {
            throw new ResponseException(
                    ResponseStatus.PRODUCT_NOT_FOUND.getCode(),
                    "Товар недоступен: " + product.getName()
            );
        }
        return product;
    }

    private ProductVariant resolveVariant(Long variantId, Product product) {
        if (variantId == null) return null;
        return variantRepository.findById(variantId)
                .filter(v -> v.getProduct().getId().equals(product.getId()))
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.BAD_REQUEST.getCode(),
                        "Вариация не принадлежит данному товару"
                ));
    }

    private CartItem findCartItem(Long itemId) {
        User user = currentUser();
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.CART_ITEM_NOT_FOUND.getCode(),
                        ResponseStatus.CART_ITEM_NOT_FOUND.getMessage()
                ));
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new ResponseException(
                    ResponseStatus.CART_ITEM_NOT_FOUND.getCode(),
                    ResponseStatus.CART_ITEM_NOT_FOUND.getMessage()
            );
        }
        return item;
    }

    private void validateStock(long available, int requested, String productName) {
        if (requested > available) {
            throw new ResponseException(
                    ResponseStatus.NO_STOCK_SPACE.getCode(),
                    ResponseStatus.NO_STOCK_SPACE.getMessage() + ": " + productName
                            + " (доступно: " + available + ")"
            );
        }
    }


    private CartDto toDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::toItemDto)
                .toList();

        return CartDto.builder()
                .id(cart.getId())
                .items(itemDtos)
                .totalItems(cart.getTotalItems())
                .totalPrice(cart.getTotalPrice())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemDto toItemDto(CartItem item) {
        String mainImage = item.getProduct().getImages() != null
                ? item.getProduct().getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null)
                : null;

        CartItemDto.CartItemDtoBuilder builder = CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .productMainImage(mainImage)
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal());

        if (item.getVariant() != null) {
            builder.variantId(item.getVariant().getId())
                    .variantSku(item.getVariant().getSku())
                    .variantAttributes(item.getVariant().getAttributes());
        }

        return builder.build();
    }
}