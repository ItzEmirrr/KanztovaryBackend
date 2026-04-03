package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.repository.OrderItemRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.OrderRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.ProductRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.UserRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.projection.DailyRevenueProjection;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.projection.DeliveryTypeCountProjection;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.projection.StatusCountProjection;
import kg.kanztovary.kanztovarybackend.domain.dto.kpi.DailyRevenueDto;
import kg.kanztovary.kanztovarybackend.domain.dto.kpi.KpiResponse;
import kg.kanztovary.kanztovarybackend.domain.dto.kpi.LowStockProductDto;
import kg.kanztovary.kanztovarybackend.domain.dto.kpi.TopProductDto;
import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import kg.kanztovary.kanztovarybackend.domain.enums.KpiPeriod;
import kg.kanztovary.kanztovarybackend.domain.enums.OrderStatus;
import kg.kanztovary.kanztovarybackend.domain.enums.ProductStatus;
import kg.kanztovary.kanztovarybackend.domain.service.KpiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiServiceImpl implements KpiService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${kpi.low-stock-threshold:5}")
    private int lowStockThreshold;

    private static final OrderStatus CANCELLED = OrderStatus.CANCELLED;

    @Override
    public KpiResponse getKpi(KpiPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = period.periodStart(now);
        LocalDateTime prevStart = period.periodStart(periodStart);

        log.info("Расчёт KPI: период={}, от={} до={}", period, periodStart, now);

        BigDecimal totalRevenue    = orderRepository.sumTotalRevenue(CANCELLED);
        BigDecimal periodRevenue   = orderRepository.sumRevenueBetween(CANCELLED, periodStart, now);
        BigDecimal prevRevenue     = orderRepository.sumRevenueBetween(CANCELLED, prevStart, periodStart);
        Double growthPercent       = calcGrowthPercent(periodRevenue, prevRevenue);
        BigDecimal avgOrderValue   = orderRepository.avgOrderValue(CANCELLED);
        BigDecimal totalDeliveryFees = orderRepository.sumDeliveryFees(CANCELLED);

        Long totalOrders  = orderRepository.count();
        Long periodOrders = orderRepository.countByPeriod(periodStart, now);

        Map<String, Long> ordersByStatus = orderRepository.countGroupByStatus()
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getStatus().name(),
                        StatusCountProjection::getCount
                ));

        long cancelledCount = ordersByStatus.getOrDefault("CANCELLED", 0L);
        double cancellationRate = totalOrders > 0
                ? BigDecimal.valueOf(cancelledCount * 100.0 / totalOrders)
                       .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        Map<DeliveryType, Long> deliveryMap = orderRepository.countGroupByDeliveryType()
                .stream()
                .collect(Collectors.toMap(
                        DeliveryTypeCountProjection::getDeliveryType,
                        DeliveryTypeCountProjection::getCount
                ));
        Long pickupCount   = deliveryMap.getOrDefault(DeliveryType.PICKUP,   0L);
        Long deliveryCount = deliveryMap.getOrDefault(DeliveryType.DELIVERY, 0L);

        List<TopProductDto> topProducts = orderItemRepository
                .findTopProducts(CANCELLED, PageRequest.of(0, 5))
                .stream()
                .map(p -> TopProductDto.builder()
                        .productId(p.getProductId())
                        .productName(p.getProductName())
                        .productSku(p.getProductSku())
                        .totalQuantity(p.getTotalQuantity())
                        .totalRevenue(p.getTotalRevenue())
                        .build())
                .toList();

        List<LowStockProductDto> lowStock = productRepository
                .findByStatusAndStockQuantityLessThanEqualOrderByStockQuantityAsc(
                        ProductStatus.ACTIVE, lowStockThreshold)
                .stream()
                .map(p -> LowStockProductDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .sku(p.getSku())
                        .stockQuantity(p.getStockQuantity())
                        .build())
                .toList();

        Long totalCustomers = userRepository.count();

        List<DailyRevenueDto> revenueChart = buildRevenueChart(
                periodStart.toLocalDate(), now.toLocalDate(),
                orderRepository.findDailyRevenue(CANCELLED.name(), periodStart, now)
        );

        return KpiResponse.builder()
                .period(period.name())
                .periodStart(periodStart.toLocalDate())
                .periodEnd(now.toLocalDate())
                // Выручка
                .totalRevenue(totalRevenue)
                .periodRevenue(periodRevenue)
                .previousPeriodRevenue(prevRevenue)
                .revenueGrowthPercent(growthPercent)
                .averageOrderValue(avgOrderValue.setScale(2, RoundingMode.HALF_UP))
                .totalDeliveryFees(totalDeliveryFees)
                // Заказы
                .totalOrders(totalOrders)
                .periodOrders(periodOrders)
                .ordersByStatus(ordersByStatus)
                .cancellationRate(cancellationRate)
                // Доставка
                .pickupCount(pickupCount)
                .deliveryCount(deliveryCount)
                // Товары
                .topProducts(topProducts)
                .lowStockProducts(lowStock)
                .lowStockThreshold(lowStockThreshold)
                // Клиенты
                .totalCustomers(totalCustomers)
                // График
                .revenueChart(revenueChart)
                .build();
    }


    /**
     * Рассчитывает процент роста выручки.
     * Возвращает {@code null} если предыдущий период равен нулю.
     */
    private Double calcGrowthPercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : null;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Строит непрерывный ряд точек для графика: заполняет пропущенные даты
     * (дни без заказов) нулями, чтобы линия на графике не прерывалась.
     */
    private List<DailyRevenueDto> buildRevenueChart(
            LocalDate from, LocalDate to,
            List<DailyRevenueProjection> dbRows) {

        Map<LocalDate, DailyRevenueProjection> byDate = dbRows.stream()
                .collect(Collectors.toMap(DailyRevenueProjection::getDay, r -> r));

        List<DailyRevenueDto> chart = new ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            DailyRevenueProjection row = byDate.get(cursor);
            chart.add(DailyRevenueDto.builder()
                    .day(cursor)
                    .revenue(row != null ? row.getRevenue() : BigDecimal.ZERO)
                    .orderCount(row != null ? row.getOrderCount() : 0L)
                    .build());
            cursor = cursor.plusDays(1);
        }
        return chart;
    }
}