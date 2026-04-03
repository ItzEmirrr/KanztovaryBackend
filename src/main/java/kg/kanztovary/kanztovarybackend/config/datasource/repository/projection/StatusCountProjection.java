package kg.kanztovary.kanztovarybackend.config.datasource.repository.projection;

import kg.kanztovary.kanztovarybackend.domain.enums.OrderStatus;

/** Количество заказов по статусу (JPQL GROUP BY) */
public interface StatusCountProjection {
    OrderStatus getStatus();
    Long getCount();
}