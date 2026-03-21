package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.domain.dto.order.*;

public interface OrderService {

    /** Создать заказ (привязывается к текущему пользователю) */
    OrderResponse create(CreateOrderRequest request);

    /** История заказов текущего пользователя с фильтрацией */
    OrderPageResponse getMyOrders(OrderFilterRequest filter);

    /** Конкретный заказ текущего пользователя (с проверкой владельца) */
    OrderResponse getMyOrderById(Long id);


    /** Все заказы с фильтрацией (только ADMIN) */
    OrderPageResponse getAllOrders(OrderFilterRequest filter);

    /** Любой заказ по ID (только ADMIN) */
    OrderResponse getById(Long id);

    /** Сменить статус заказа (только ADMIN) */
    OrderResponse updateStatus(Long id, UpdateOrderRequest request);

    /** Удалить заказ (только ADMIN) */
    void delete(Long id);
}