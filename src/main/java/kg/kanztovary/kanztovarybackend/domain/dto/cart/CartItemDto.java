package kg.kanztovary.kanztovarybackend.domain.dto.cart;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {

    private Long id;

    private Long productId;
    private String productName;
    private String productSku;
    private String productMainImage;

    private Long variantId;
    private String variantSku;
    private Map<String, String> variantAttributes;

    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}