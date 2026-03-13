package kg.kanztovary.kanztovarybackend.domain.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class ProductVariantRequestDto {

    @NotBlank(message = "Артикул вариации обязателен")
    @Size(max = 100, message = "Артикул не должен превышать 100 символов")
    private String sku;

    /** Штрих-код вариации (необязательный, уникальный) */
    @Size(max = 100, message = "Штрих-код не должен превышать 100 символов")
    private String barcode;

    /** Если не указана — используется цена основного товара */
    @PositiveOrZero(message = "Цена не может быть отрицательной")
    private BigDecimal price;

    @PositiveOrZero(message = "Количество не может быть отрицательным")
    private Long stockQuantity;

    /** Атрибуты: {"color": "красный", "size": "A4"} */
    private Map<String, String> attributes;
}