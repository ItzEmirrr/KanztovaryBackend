package kg.kanztovary.kanztovarybackend.domain.dto.productImage;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto {
    private String imageUrl;
    private String altText;
    private Boolean isMain;
    private Integer sortOrder;
}