package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import jakarta.persistence.criteria.JoinType;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.Product;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.ProductVariant;
import kg.kanztovary.kanztovarybackend.domain.dto.product.ProductFilterRequest;
import kg.kanztovary.kanztovarybackend.domain.enums.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Фабрика JPA-спецификаций для поиска и фильтрации товаров.
 *
 * <p>Каждый метод возвращает одно условие. Если параметр не задан — возвращает
 * {@code null}, что Spring Data интерпретирует как «условие отсутствует» (игнорирует).
 */
public class ProductSpecification {

    private ProductSpecification() {}

    /**
     * Собирает итоговую спецификацию из всех параметров фильтра.
     *
     * @param filter         параметры фильтрации из запроса
     * @param effectiveStatus статус, применяемый к выборке:
     *                        {@code ACTIVE} — для обычных пользователей,
     *                        конкретный статус или {@code null} (всё) — для админа
     */
    public static Specification<Product> withFilters(ProductFilterRequest filter,
                                                     ProductStatus effectiveStatus) {
        List<Specification<Product>> specs = new ArrayList<>();

        specs.add(hasStatus(effectiveStatus));
        specs.add(hasSearch(filter.getSearch()));
        specs.add(inCategory(filter.getCategoryId()));
        specs.add(hasBrand(filter.getBrandId()));
        specs.add(priceAtLeast(filter.getMinPrice()));
        specs.add(priceAtMost(filter.getMaxPrice()));
        specs.add(isInStock(filter.getInStock()));

        specs.removeIf(Objects::isNull);

        if (specs.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return Specification.allOf(specs.toArray(new Specification[0]));
    }


    /** Фильтр по статусу. {@code null} → статус не ограничивается (для admin). */
    private static Specification<Product> hasStatus(ProductStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Полнотекстовый поиск (LIKE, без учёта регистра) по названию, описанию и SKU.
     * Пустая/null строка → предикат не добавляется.
     */
    private static Specification<Product> hasSearch(String search) {
        if (search == null || search.isBlank()) return null;
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern),
                cb.like(cb.lower(root.get("sku")), pattern),
                cb.like(cb.lower(root.get("barcode")), pattern)
        );
    }

    /**
     * Фильтр по категории (JOIN).
     * {@code query.distinct(true)} предотвращает дублирование товаров,
     * принадлежащих нескольким категориям; для count-запроса тоже корректно
     * (COUNT DISTINCT).
     */
    private static Specification<Product> inCategory(Integer categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                query.distinct(true);
            }
            var categories = root.join("categories", JoinType.INNER);
            return cb.equal(categories.get("id"), categoryId);
        };
    }

    /** Фильтр по бренду */
    private static Specification<Product> hasBrand(Long brandId) {
        if (brandId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId);
    }

    /** Минимальная цена (включительно) */
    private static Specification<Product> priceAtLeast(BigDecimal minPrice) {
        if (minPrice == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    /** Максимальная цена (включительно) */
    private static Specification<Product> priceAtMost(BigDecimal maxPrice) {
        if (maxPrice == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    /**
     * Только товары в наличии.
     * Товар считается «в наличии», если:
     * <ul>
     *   <li>его собственный {@code stockQuantity > 0}, ИЛИ</li>
     *   <li>хотя бы одна его вариация имеет {@code stockQuantity > 0}.</li>
     * </ul>
     */
    private static Specification<Product> isInStock(Boolean inStock) {
        if (inStock == null || !inStock) return null;
        return (root, query, cb) -> {
            // Подзапрос: есть ли вариация с ненулевым остатком
            var sub = query.subquery(Long.class);
            var variant = sub.from(ProductVariant.class);
            sub.select(variant.get("id"))
               .where(
                   cb.equal(variant.get("product"), root),
                   cb.greaterThan(variant.get("stockQuantity"), 0L)
               );

            return cb.or(
                    cb.greaterThan(root.get("stockQuantity"), 0L),
                    cb.exists(sub)
            );
        };
    }
}