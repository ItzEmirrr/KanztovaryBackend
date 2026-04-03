package kg.kanztovary.kanztovarybackend.config.datasource.repository.projection;

import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;

/** Количество заказов по типу доставки (JPQL GROUP BY) */
public interface DeliveryTypeCountProjection {
    DeliveryType getDeliveryType();
    Long getCount();
}