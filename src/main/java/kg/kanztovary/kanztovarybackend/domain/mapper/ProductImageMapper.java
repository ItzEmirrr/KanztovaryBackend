package kg.kanztovary.kanztovarybackend.domain.mapper;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Product;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.ProductImage;
import kg.kanztovary.kanztovarybackend.domain.dto.productImage.ProductImageDto;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {

    public ProductImage toEntity(Product product, ProductImageDto dto) {
        return ProductImage.builder()
                .product(product)
                .imageUrl(dto.getImageUrl())
                .altText(dto.getAltText())
                .isMain(dto.getIsMain() != null ? dto.getIsMain() : false)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
    }

    public ProductImageDto toDto(ProductImage image) {
        return ProductImageDto.builder()
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .isMain(image.getIsMain())
                .sortOrder(image.getSortOrder())
                .build();
    }
}