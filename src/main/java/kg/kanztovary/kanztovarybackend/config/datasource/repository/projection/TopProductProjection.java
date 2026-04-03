package kg.kanztovary.kanztovarybackend.config.datasource.repository.projection;

import java.math.BigDecimal;

/** Топ товар по объёму продаж (JPQL GROUP BY на OrderItem) */
public interface TopProductProjection {
    Long getProductId();
    String getProductName();
    String getProductSku();
    Long getTotalQuantity();
    BigDecimal getTotalRevenue();
}