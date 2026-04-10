package kg.kanztovary.kanztovarybackend.domain.dto.ai;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendationRequest {

    @NotBlank(message = "Запрос не может быть пустым")
    @Size(max = 500, message = "Запрос не должен превышать 500 символов")
    private String query;

    @Min(1)
    @Max(10)
    private int maxResults = 5;
}