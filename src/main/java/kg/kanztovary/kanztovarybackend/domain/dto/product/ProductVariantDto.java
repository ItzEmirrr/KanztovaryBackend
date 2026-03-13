package kg.kanztovary.kanztovarybackend.domain.dto.product;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDto {
    private Long id;
    private String sku;
    private String barcode;
    /** Собственная цена вариации (null — используется цена товара) */
    private BigDecimal price;
    /** Итоговая цена с учётом родительского товара */
    private BigDecimal effectivePrice;
    private Long stockQuantity;
    private Map<String, String> attributes;
}