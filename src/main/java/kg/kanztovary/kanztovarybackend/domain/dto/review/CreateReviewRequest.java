package kg.kanztovary.kanztovarybackend.domain.dto.review;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequest {

    @NotNull(message = "Оценка обязательна")
    @Min(value = 1, message = "Минимальная оценка — 1")
    @Max(value = 5, message = "Максимальная оценка — 5")
    private Short rating;

    @Size(max = 1000, message = "Комментарий не должен превышать 1000 символов")
    private String comment;
}