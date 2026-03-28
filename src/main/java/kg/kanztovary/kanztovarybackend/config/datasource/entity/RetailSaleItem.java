package kg.kanztovary.kanztovarybackend.config.datasource.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "retail_sale_items", schema = "stationery")
public class RetailSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retail_sale_id", nullable = false)
    private RetailSale retailSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Вариация товара. null — если продавался товар без вариации.
     * При удалении вариации из БД обнуляется (ON DELETE SET NULL).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private ProductVariant variant;

    @Column(nullable = false)
    private Long quantity;

    /** Цена товара на момент продажи (снимок) */
    @Column(name = "price_at_sale", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceAtSale;

    /** quantity × priceAtSale */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;
}