package kg.kanztovary.kanztovarybackend.domain.dto.kpi;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LowStockProductDto {
    private Long id;
    private String name;
    private String sku;
    private Long stockQuantity;
}