package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.OrderItem;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.projection.TopProductProjection;
import kg.kanztovary.kanztovarybackend.domain.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    /**
     * Топ товаров по суммарному количеству единиц в неотменённых заказах.
     * Используй с {@code PageRequest.of(0, 5)} для получения топ-5.
     */
    @Query("""
            SELECT oi.product.id       AS productId,
                   oi.product.name     AS productName,
                   oi.product.sku      AS productSku,
                   SUM(oi.quantity)    AS totalQuantity,
                   SUM(oi.price)       AS totalRevenue
            FROM OrderItem oi
            WHERE oi.order.status != :excluded
            GROUP BY oi.product.id, oi.product.name, oi.product.sku
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<TopProductProjection> findTopProducts(
            @Param("excluded") OrderStatus excluded,
            Pageable pageable
    );
}