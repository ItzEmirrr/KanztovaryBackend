package kg.kanztovary.kanztovarybackend.domain.enums;

/**
 * Способ получения заказа.
 * Передаётся клиентом при оформлении: ?deliveryType=DELIVERY
 */
public enum DeliveryType {
    /** Клиент забирает заказ самостоятельно из магазина */
    PICKUP,
    /** Доставка по адресу; стоимость зависит от суммы заказа */
    DELIVERY
}
