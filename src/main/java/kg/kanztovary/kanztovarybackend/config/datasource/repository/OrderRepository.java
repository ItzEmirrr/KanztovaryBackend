package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Order;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.projection.DailyRevenueProjection;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.projection.DeliveryTypeCountProjection;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.projection.StatusCountProjection;
import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import kg.kanztovary.kanztovarybackend.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    @EntityGraph(attributePaths = {"items", "items.product", "user", "statusHistory"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findDetailById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items", "items.product", "statusHistory"})
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findByIdAndUserId(@Param("id") Long id,
                                      @Param("userId") Integer userId);


    @Query("""
            SELECT o FROM Order o
            WHERE o.user.id = :userId
              AND (CAST(:status AS string) IS NULL OR o.status = :status)
              AND (CAST(:from AS timestamp) IS NULL OR o.createdAt >= :from)
              AND (CAST(:to AS timestamp) IS NULL OR o.createdAt <= :to)
            ORDER BY o.createdAt DESC
            """)
    Page<Order> findByUser(
            @Param("userId") Integer userId,
            @Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );


    @Query("""
            SELECT o FROM Order o
            WHERE (o.status = COALESCE(:status, o.status))
              AND (o.deliveryType = COALESCE(:deliveryType, o.deliveryType))
              AND (o.user.id = COALESCE(:userId, o.user.id))
              AND (o.createdAt >= COALESCE(:from, o.createdAt))
              AND (o.createdAt <= COALESCE(:to, o.createdAt))
            ORDER BY o.createdAt DESC
            """)
    Page<Order> findAllWithFilters(
            @Param("status") OrderStatus status,
            @Param("deliveryType") DeliveryType deliveryType,
            @Param("userId") Integer userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );


    /** Общая выручка за всё время (кроме отменённых) */
    @Query("""
            SELECT COALESCE(SUM(o.totalPrice + COALESCE(o.deliveryFee, 0)), 0)
            FROM Order o WHERE o.status != :excluded
            """)
    BigDecimal sumTotalRevenue(@Param("excluded") OrderStatus excluded);

    /** Выручка за период (кроме отменённых) */
    @Query("""
            SELECT COALESCE(SUM(o.totalPrice + COALESCE(o.deliveryFee, 0)), 0)
            FROM Order o
            WHERE o.status != :excluded
              AND o.createdAt >= :from
              AND o.createdAt <= :to
            """)
    BigDecimal sumRevenueBetween(
            @Param("excluded") OrderStatus excluded,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /** Средний чек по всем неотменённым заказам */
    @Query("""
            SELECT COALESCE(AVG(o.totalPrice + COALESCE(o.deliveryFee, 0)), 0)
            FROM Order o WHERE o.status != :excluded
            """)
    BigDecimal avgOrderValue(@Param("excluded") OrderStatus excluded);

    /** Суммарно собрано за доставку (кроме отменённых) */
    @Query("""
            SELECT COALESCE(SUM(COALESCE(o.deliveryFee, 0)), 0)
            FROM Order o WHERE o.status != :excluded
            """)
    BigDecimal sumDeliveryFees(@Param("excluded") OrderStatus excluded);


    /** Количество заказов за период */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from AND o.createdAt <= :to")
    Long countByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Количество заказов по статусам */
    @Query("SELECT o.status as status, COUNT(o) as count FROM Order o GROUP BY o.status")
    List<StatusCountProjection> countGroupByStatus();


    /** Количество заказов по типу доставки */
    @Query("SELECT o.deliveryType as deliveryType, COUNT(o) as count FROM Order o GROUP BY o.deliveryType")
    List<DeliveryTypeCountProjection> countGroupByDeliveryType();

    // ─── KPI: График ─────────────────────────────────────────────────────────

    /**
     * Ежедневная выручка за период (native SQL — JPQL не поддерживает DATE()).
     * Дни без заказов НЕ включены: сервис заполняет пробелы нулями сам.
     */
    @Query(value = """
            SELECT CAST(o.created_at AS DATE)                        AS day,
                   COALESCE(SUM(o.total_price + o.delivery_fee), 0) AS revenue,
                   COUNT(o.id)                                       AS order_count
            FROM stationery.orders o
            WHERE o.status != :excludedStatus
              AND o.created_at >= :from
              AND o.created_at <= :to
            GROUP BY CAST(o.created_at AS DATE)
            ORDER BY CAST(o.created_at AS DATE)
            """,
            nativeQuery = true)
    List<DailyRevenueProjection> findDailyRevenue(
            @Param("excludedStatus") String excludedStatus,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}