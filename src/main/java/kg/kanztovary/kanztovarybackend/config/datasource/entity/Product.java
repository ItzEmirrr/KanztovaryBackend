package kg.kanztovary.kanztovarybackend.config.datasource.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import kg.kanztovary.kanztovarybackend.domain.enums.ProductStatus;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "products", schema = "stationery")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Positive
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /** Цена со скидкой. Если null — скидки нет */
    @PositiveOrZero
    @Column(name = "discount_price", precision = 15, scale = 2)
    private BigDecimal discountPrice;

    /** Уникальный артикул товара (Stock Keeping Unit) */
    @NotBlank
    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    /** Штрих-код (EAN-13, Code-128 и т.д.). Необязательный, уникальный. */
    @Column(name = "barcode", unique = true, length = 100)
    private String barcode;

    /**
     * Остаток на складе — используется, когда у товара нет вариаций.
     * Если вариации есть, актуальный остаток берётся из ProductVariant.
     */
    @PositiveOrZero
    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private Long stockQuantity = 0L;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 50)
    private ProductStatus status = ProductStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Необязательно */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /** Категории товара (один товар может быть в нескольких категориях) */
    @BatchSize(size = 20)
    @ManyToMany
    @Builder.Default
    @JoinTable(
            name = "product_categories",
            schema = "stationery",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants;

    /**
     * Изображения товара.
     * @BatchSize — предотвращает N+1 когда товары загружаются списком
     * (например в заказе: 20 товаров → 1 пакетный запрос на изображения).
     */
    @BatchSize(size = 10)
    @OrderBy("sortOrder ASC")
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> images;
}