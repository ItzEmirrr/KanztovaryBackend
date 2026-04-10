package kg.kanztovary.kanztovarybackend.domain.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {

    @NotNull(message = "ID товара обязателен")
    private Long productId;

    /**
     * ID вариации товара (цвет, размер и т.д.).
     * Если не указан — добавляется базовый товар.
     */
    private Long variantId;

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть не менее 1")
    private Integer quantity;
}