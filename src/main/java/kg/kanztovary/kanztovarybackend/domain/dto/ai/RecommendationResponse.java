package kg.kanztovary.kanztovarybackend.domain.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private String summary;
    private List<RecommendedProduct> recommendations;
}