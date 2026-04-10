package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.domain.dto.cart.AddToCartRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.cart.CartDto;
import kg.kanztovary.kanztovarybackend.domain.dto.cart.CheckoutRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.cart.UpdateCartItemRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.order.OrderResponse;

public interface CartService {

    /** Получить корзину текущего пользователя (создаёт если нет) */
    CartDto getCart();

    /** Добавить товар в корзину (или увеличить количество) */
    CartDto addItem(AddToCartRequest request);

    /** Изменить количество позиции в корзине */
    CartDto updateItem(Long itemId, UpdateCartItemRequest request);

    /** Удалить одну позицию из корзины */
    CartDto removeItem(Long itemId);

    /** Очистить корзину (удалить все позиции) */
    void clearCart();

    /**
     * Оформить заказ из корзины.
     * Клиент указывает способ получения, адрес (при доставке) и телефон.
     */
    OrderResponse checkout(CheckoutRequest request);
}