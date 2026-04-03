package kg.kanztovary.kanztovarybackend.config.datasource.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Дневная выручка для графика (native SQL GROUP BY DATE) */
public interface DailyRevenueProjection {
    LocalDate getDay();
    BigDecimal getRevenue();
    Long getOrderCount();
}