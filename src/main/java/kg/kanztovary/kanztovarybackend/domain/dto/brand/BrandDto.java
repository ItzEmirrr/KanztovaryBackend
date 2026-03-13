package kg.kanztovary.kanztovarybackend.domain.dto.brand;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrandDto {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String websiteUrl;
}