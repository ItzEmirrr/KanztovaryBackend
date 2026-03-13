package kg.kanztovary.kanztovarybackend.domain.dto.product;

import lombok.Builder;
import lombok.Getter;

/**
 * Ответ на поиск по штрих-коду или SKU.
 *
 * <p>Если штрих-код принадлежит вариации — {@code matchedVariantId} содержит её ID,
 * и фронт должен автоматически выбрать эту вариацию в корзине кассы.
 * Если штрих-код принадлежит базовому товару — {@code matchedVariantId} равен null.
 */
@Getter
@Builder
public class BarcodeSearchResponse {

    /** Полные данные о товаре вместе со всеми вариациями */
    private ProductDto product;

    /**
     * ID вариации, чей штрих-код был отсканирован.
     * null — если совпал штрих-код самого товара, а не вариации.
     */
    private Long matchedVariantId;
}