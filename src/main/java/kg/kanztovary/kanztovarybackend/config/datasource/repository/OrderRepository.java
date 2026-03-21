package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Order;
import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import kg.kanztovary.kanztovarybackend.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
}