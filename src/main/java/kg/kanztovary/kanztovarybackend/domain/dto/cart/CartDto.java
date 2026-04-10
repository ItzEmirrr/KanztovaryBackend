package kg.kanztovary.kanztovarybackend.domain.dto.cart;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {

    private Long id;
    private List<CartItemDto> items;

    /** Общее кол-во единиц товара (с учётом quantity каждой позиции) */
    private int totalItems;

    /** Итоговая сумма корзины */
    private BigDecimal totalPrice;

    private LocalDateTime updatedAt;
}