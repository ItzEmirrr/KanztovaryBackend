package kg.kanztovary.kanztovarybackend.domain.dto.kpi;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class TopProductDto {
    private Long productId;
    private String productName;
    private String productSku;
    /** Суммарное количество единиц в выполненных / активных заказах */
    private Long totalQuantity;
    /** Суммарная выручка по этому товару */
    private BigDecimal totalRevenue;
}