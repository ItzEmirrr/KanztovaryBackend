package kg.kanztovary.kanztovarybackend.domain.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Данные для создания / обновления товара.
 *
 * <p>Передаётся как часть {@code multipart/form-data} запроса:
 * <pre>
 * POST /api/v1/products
 * Content-Type: multipart/form-data
 *
 * data:   { ...поля этого DTO в виде JSON... }
 * images: file1.jpg
 * images: file2.png
 * </pre>
 *
 * Изображения приходят отдельными файлами — сервер сам выставляет
 * {@code sortOrder} и {@code isMain} (первый файл = главное изображение).
 */
@Getter
@Setter
@ToString
public class ProductRequestDto {

    @NotBlank(message = "Название товара обязательно")
    private String name;

    private String description;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть больше нуля")
    private BigDecimal price;

    @PositiveOrZero(message = "Скидочная цена не может быть отрицательной")
    private BigDecimal discountPrice;

    @NotBlank(message = "Артикул (SKU) обязателен")
    @Size(max = 100, message = "Артикул не должен превышать 100 символов")
    private String sku;

    /** Штрих-код (EAN-13, Code-128 и т.д.). Необязательный, уникальный. */
    @Size(max = 100, message = "Штрих-код не должен превышать 100 символов")
    private String barcode;

    @PositiveOrZero(message = "Количество на складе не может быть отрицательным")
    private Long stockQuantity;

    /** ID бренда (необязательно) */
    private Long brandId;

    /** IDs категорий (минимум одна) */
    @NotEmpty(message = "Укажите хотя бы одну категорию")
    private Set<Integer> categoryIds;

    @Valid
    private List<ProductVariantRequestDto> variants;
}
