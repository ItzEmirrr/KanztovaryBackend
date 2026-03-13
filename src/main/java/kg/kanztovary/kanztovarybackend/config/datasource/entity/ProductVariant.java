package kg.kanztovary.kanztovarybackend.config.datasource.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "product_variants", schema = "stationery")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Уникальный артикул конкретной вариации. */
    @NotBlank
    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    /** Штрих-код этой конкретной вариации (EAN-13, Code-128 и т.д.) */
    @Column(name = "barcode", unique = true, length = 100)
    private String barcode;

    /** Цена вариации. Если null — используется цена основного товара. */
    @PositiveOrZero
    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

    /** Остаток на складе для данной вариации */
    @PositiveOrZero
    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private Long stockQuantity = 0L;

    /**
     * Атрибуты вариации в формате ключ-значение.
     * Примеры: {"color": "синий", "size": "M"}, {"material": "пластик"}
     */
    @ElementCollection
    @CollectionTable(
            name = "product_variant_attributes",
            schema = "stationery",
            joinColumns = @JoinColumn(name = "variant_id")
    )
    @MapKeyColumn(name = "attribute_key", length = 100)
    @Column(name = "attribute_value", length = 255)
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Возвращает актуальную цену: свою, либо цену родительского товара */
    public BigDecimal getEffectivePrice() {
        return price != null ? price : product.getPrice();
    }
}