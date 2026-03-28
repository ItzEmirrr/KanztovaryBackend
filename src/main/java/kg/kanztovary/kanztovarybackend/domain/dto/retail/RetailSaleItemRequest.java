package kg.kanztovary.kanztovarybackend.domain.dto.retail;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetailSaleItemRequest {

    @NotNull(message = "ID товара обязателен")
    private Long productId;

    /**
     * ID вариации (необязательно).
     * Если указан — списание идёт из остатка вариации, иначе из остатка товара.
     */
    private Long variantId;

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть не менее 1")
    private Long quantity;
}