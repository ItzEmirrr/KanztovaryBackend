package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.domain.dto.cart.AddToCartRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.cart.CartDto;
import kg.kanztovary.kanztovarybackend.domain.dto.cart.CheckoutRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.cart.UpdateCartItemRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.order.OrderResponse;
import kg.kanztovary.kanztovarybackend.domain.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** Получить текущую корзину */
    @GetMapping
    public CartDto getCart() {
        return cartService.getCart();
    }

    /** Добавить товар (или увеличить количество) */
    @PostMapping("/items")
    public CartDto addItem(@Valid @RequestBody AddToCartRequest request) {
        log.info("Добавление в корзину: productId={}, variantId={}, qty={}",
                request.getProductId(), request.getVariantId(), request.getQuantity());
        return cartService.addItem(request);
    }

    /** Изменить количество позиции */
    @PutMapping("/items/{itemId}")
    public CartDto updateItem(@PathVariable Long itemId,
                              @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Обновление позиции корзины id={}, qty={}", itemId, request.getQuantity());
        return cartService.updateItem(itemId, request);
    }

    /** Удалить одну позицию из корзины */
    @DeleteMapping("/items/{itemId}")
    public CartDto removeItem(@PathVariable Long itemId) {
        log.info("Удаление позиции корзины id={}", itemId);
        return cartService.removeItem(itemId);
    }

    /** Очистить корзину */
    @DeleteMapping
    public void clearCart() {
        log.info("Очистка корзины");
        cartService.clearCart();
    }

    /**
     * Оформить заказ из корзины.
     *
     * <pre>
     * // Самовывоз
     * POST /api/v1/cart/checkout
     * { "deliveryType": "PICKUP", "phoneNumber": "+996700123456" }
     *
     * // Доставка
     * POST /api/v1/cart/checkout
     * { "deliveryType": "DELIVERY",
     *   "deliveryAddress": "г. Бишкек, ул. Манаса 1, кв. 5",
     *   "phoneNumber": "+996700123456" }
     * </pre>
     */
    @PostMapping("/checkout")
    public OrderResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        log.info("Оформление заказа из корзины: способ={}", request.getDeliveryType());
        return cartService.checkout(request);
    }
}