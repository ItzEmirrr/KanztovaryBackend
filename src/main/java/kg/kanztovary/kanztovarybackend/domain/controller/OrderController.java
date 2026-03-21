package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import kg.kanztovary.kanztovarybackend.domain.dto.order.*;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    /** Создать заказ напрямую (минуя корзину). */
    @PostMapping
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Создание заказа, позиций: {}", request.getItems().size());
        return service.create(request);
    }

    /**
     * История заказов текущего пользователя.
     * ?status=COMPLETED&from=2024-01-01&to=2024-12-31&page=0&size=10
     */
    @GetMapping("/my")
    public OrderPageResponse getMyOrders(@ModelAttribute OrderFilterRequest filter) {
        return service.getMyOrders(filter);
    }

    /** Конкретный заказ текущего пользователя (чужой → 400). */
    @GetMapping("/my/{id}")
    public OrderResponse getMyOrderById(@PathVariable Long id) {
        return service.getMyOrderById(id);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Административные эндпоинты — только ADMIN
    // ═══════════════════════════════════════════════════════════════════════

    /** Все заказы с фильтрами. ?status=NEW&userId=5&from=2024-01-01&page=0&size=20 */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public OrderPageResponse getAllOrders(@ModelAttribute OrderFilterRequest filter) {
        return service.getAllOrders(filter);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @RequestBody UpdateOrderRequest request) {
        log.info("Смена статуса заказа #{} → {}", id, request.getStatus());
        return service.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDataDto delete(@PathVariable Long id) {
        log.info("Удаление заказа #{}", id);
        service.delete(id);
        return ResponseDataDto.builder()
                .code(ResponseStatus.SUCCESS.getCode())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }
}