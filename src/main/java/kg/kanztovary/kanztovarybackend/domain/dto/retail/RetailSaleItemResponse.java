package kg.kanztovary.kanztovarybackend.domain.dto.retail;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RetailSaleItemResponse {

    private Long productId;
    private String productName;
    private String sku;
    private String barcode;

    /** Заполнен, если продавалась конкретная вариация */
    private Long variantId;
    private String variantSku;
    private String variantBarcode;
    private java.util.Map<String, String> variantAttributes;

    private Long quantity;
    private BigDecimal priceAtSale;
    private BigDecimal subtotal;
}
