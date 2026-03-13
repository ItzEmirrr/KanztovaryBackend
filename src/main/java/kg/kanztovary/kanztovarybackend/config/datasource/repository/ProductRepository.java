package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Product;
import kg.kanztovary.kanztovarybackend.domain.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    // ─── Поиск с фильтрами (пагинация) ───────────────────────────────────────
    //
    // @EntityGraph здесь НЕ используется намеренно: при JOIN FETCH + LIMIT
    // Hibernate переносит ограничение в память (HHH90003004).
    // Вместо этого коллекции (categories, images, variants) подгружаются
    // пакетами через @BatchSize на сущностях — без N+1 и без этой проблемы.
    @Override
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);



    /** Все активные товары — например, для экспорта или фоновых задач */
    @EntityGraph(attributePaths = {"images", "categories", "brand"})
    List<Product> findAllByStatus(ProductStatus status);

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    /**
     * Активные товары с остатком ≤ порогу, отсортированные по возрастанию остатка.
     * Используй с {@code PageRequest.of(0, N)} для ограничения списка.
     */
    List<Product> findByStatusAndStockQuantityLessThanEqualOrderByStockQuantityAsc(
            ProductStatus status, long threshold
    );
}