package kg.kanztovary.kanztovarybackend.domain.dto.kpi;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KpiResponse {

    private String period;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    /** Общая выручка за всё время (кроме CANCELLED) */
    private BigDecimal totalRevenue;

    /** Выручка за выбранный период */
    private BigDecimal periodRevenue;

    /** Выручка за предыдущий аналогичный период (для сравнения) */
    private BigDecimal previousPeriodRevenue;

    /**
     * Рост выручки в % относительно предыдущего периода.
     * null — если предыдущий период был нулевым.
     */
    private Double revenueGrowthPercent;

    /** Средний чек (по ненулевым неотменённым заказам) */
    private BigDecimal averageOrderValue;

    /** Суммарно собрано за доставку за всё время */
    private BigDecimal totalDeliveryFees;


    private Long totalOrders;

    /** Заказов за выбранный период */
    private Long periodOrders;

    /** Количество заказов по каждому статусу.*/
    private Map<String, Long> ordersByStatus;

    /** Процент отменённых заказов от общего числа */
    private Double cancellationRate;


    /** Количество заказов с самовывозом */
    private Long pickupCount;

    /** Количество заказов с доставкой */
    private Long deliveryCount;

    // ─── Товары ───────────────────────────────────────────────────────────────

    /** Топ-5 товаров по количеству проданных единиц */
    private List<TopProductDto> topProducts;

    /** Товары с критически низким остатком (≤ порога из конфига).*/
    private List<LowStockProductDto> lowStockProducts;

    /** Порог «низкий остаток» (из application.properties) */
    private Integer lowStockThreshold;


    /** Всего зарегистрированных пользователей */
    private Long totalCustomers;

    /**
     * Ежедневная выручка за выбранный период.
     * Дни без заказов включены с revenue=0 для непрерывного графика.
     */
    private List<DailyRevenueDto> revenueChart;
}