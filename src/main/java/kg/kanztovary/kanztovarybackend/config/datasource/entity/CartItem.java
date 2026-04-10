package kg.kanztovary.kanztovarybackend.config.datasource.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "cart_items", schema = "stationery")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Конкретная вариация товара (цвет, размер и т.д.).
     * Null — если товар без вариаций или выбран базовый вариант.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Min(1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;


    /**
     * Актуальная цена за единицу:
     * берётся из вариации (если есть), иначе из товара.
     */
    public BigDecimal getUnitPrice() {
        if (variant != null && variant.getPrice() != null) {
            return variant.getPrice();
        }
        if (product.getDiscountPrice() != null) {
            return product.getDiscountPrice();
        }
        return product.getPrice();
    }

    /** Стоимость позиции (цена × количество) */
    public BigDecimal getSubtotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }
}