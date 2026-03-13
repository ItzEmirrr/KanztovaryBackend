package kg.kanztovary.kanztovarybackend.domain.dto.product;

import kg.kanztovary.kanztovarybackend.domain.enums.ProductSortBy;
import kg.kanztovary.kanztovarybackend.domain.enums.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Параметры поиска и фильтрации каталога товаров.
 *
 * <pre>
 * GET /api/v1/products?search=parker&categoryId=2&brandId=1
 *                     &minPrice=500&maxPrice=5000&inStock=true
 *                     &sortBy=PRICE_ASC&page=0&size=12
 * </pre>
 */
@Getter
@Setter
public class ProductFilterRequest {

    /** Полнотекстовый поиск по названию, описанию, SKU */
    private String search;

    /** Фильтр по категории */
    private Integer categoryId;

    /** Фильтр по бренду */
    private Long brandId;

    /** Минимальная цена (включительно) */
    private BigDecimal minPrice;

    /** Максимальная цена (включительно) */
    private BigDecimal maxPrice;

    /**
     * Только товары в наличии (stockQuantity > 0).
     * Если у товара есть вариации — проверяется суммарный остаток по ним.
     */
    private Boolean inStock;

    /**
     * Статус товара — только для ADMIN.
     * Для обычных пользователей всегда используется ACTIVE.
     */
    private ProductStatus status;

    /** Способ сортировки (по умолчанию — сначала новые) */
    private ProductSortBy sortBy = ProductSortBy.NEWEST;

    /** Номер страницы, 0-based */
    private int page = 0;

    /** Размер страницы */
    private int size = 12;
}