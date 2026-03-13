package kg.kanztovary.kanztovarybackend.domain.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequestDto {

    @NotBlank(message = "Название категории обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    private String description;

    @Size(max = 100, message = "Slug не должен превышать 100 символов")
    private String slug;

    /** Для подкатегорий, необязательно */
    private Integer parentCategoryId;
}