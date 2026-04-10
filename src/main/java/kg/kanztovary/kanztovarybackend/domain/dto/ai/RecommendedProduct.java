package kg.kanztovary.kanztovarybackend.domain.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedProduct {
    private Long productId;
    private String name;
    private String reason;
}