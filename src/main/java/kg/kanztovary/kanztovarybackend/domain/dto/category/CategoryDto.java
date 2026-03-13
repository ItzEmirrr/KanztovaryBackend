package kg.kanztovary.kanztovarybackend.domain.dto.category;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDto {
    private Integer id;
    private String name;
    private String description;
    private String slug;
    private Integer parentCategoryId;
    private String parentCategoryName;
}