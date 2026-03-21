package kg.kanztovary.kanztovarybackend.domain.dto.orderitem;

import kg.kanztovary.kanztovarybackend.domain.dto.product.ProductDto;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {
    private ProductDto product;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}