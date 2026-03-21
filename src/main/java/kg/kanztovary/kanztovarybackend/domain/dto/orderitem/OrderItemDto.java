package kg.kanztovary.kanztovarybackend.domain.dto.orderitem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {
    private Long productId;
    private Integer quantity;
}