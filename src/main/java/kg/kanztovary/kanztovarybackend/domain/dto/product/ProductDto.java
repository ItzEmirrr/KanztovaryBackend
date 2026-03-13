package kg.kanztovary.kanztovarybackend.domain.dto.product;

import kg.kanztovary.kanztovarybackend.domain.dto.brand.BrandDto;
import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryDto;
import kg.kanztovary.kanztovarybackend.domain.dto.productImage.ProductImageDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String sku;
    private String barcode;
    private Long stockQuantity;
    private String status;
    private BrandDto brand;
    private Set<CategoryDto> categories;
    private List<ProductVariantDto> variants;
    private List<ProductImageDto> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}