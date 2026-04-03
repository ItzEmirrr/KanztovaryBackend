package kg.kanztovary.kanztovarybackend.domain.enums;

import java.time.LocalDateTime;

/**
 * Период агрегации для KPI.
 * Передаётся как query-параметр: GET /api/v1/admin/kpi?period=MONTH
 */
public enum KpiPeriod {
    WEEK, MONTH, YEAR;

    /** Начало текущего периода относительно заданного момента времени */
    public LocalDateTime periodStart(LocalDateTime anchor) {
        return switch (this) {
            case WEEK  -> anchor.minusWeeks(1);
            case MONTH -> anchor.minusMonths(1);
            case YEAR  -> anchor.minusYears(1);
        };
    }
}