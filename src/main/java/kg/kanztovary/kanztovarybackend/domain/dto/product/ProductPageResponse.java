package kg.kanztovary.kanztovarybackend.domain.dto.product;

import lombok.*;

import java.util.List;

/** Обёртка с пагинацией для каталога товаров */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPageResponse {
    private List<ProductDto> products;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
