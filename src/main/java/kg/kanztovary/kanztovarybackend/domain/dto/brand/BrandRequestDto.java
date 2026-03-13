package kg.kanztovary.kanztovarybackend.domain.dto.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandRequestDto {

    @NotBlank(message = "Название бренда обязательно")
    @Size(max = 100, message = "Название бренда не должно превышать 100 символов")
    private String name;

    private String description;
    private String logoUrl;
    private String websiteUrl;
}