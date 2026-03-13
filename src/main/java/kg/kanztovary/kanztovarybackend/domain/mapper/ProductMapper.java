package kg.kanztovary.kanztovarybackend.domain.mapper;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Brand;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.Category;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.Product;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.ProductVariant;
import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryDto;
import kg.kanztovary.kanztovarybackend.domain.dto.product.ProductDto;
import kg.kanztovary.kanztovarybackend.domain.dto.product.ProductRequestDto;
import kg.kanztovary.kanztovarybackend.domain.dto.product.ProductVariantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final BrandMapper brandMapper;
    private final ProductImageMapper imageMapper;

    public ProductDto toResponse(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus().name())
                .brand(brandMapper.toDto(product.getBrand()))
                .categories(mapCategories(product.getCategories()))
                .variants(mapVariants(product.getVariants()))
                .images(
                        product.getImages() != null
                                ? product.getImages().stream().map(imageMapper::toDto).toList()
                                : Collections.emptyList()
                )
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public Product toEntity(ProductRequestDto request, Set<Category> categories, Brand brand) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .sku(request.getSku())
                .barcode(request.getBarcode())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0L)
                .brand(brand)
                .categories(categories)
                .build();
    }


    private Set<CategoryDto> mapCategories(Set<Category> categories) {
        if (categories == null) return Collections.emptySet();
        return categories.stream()
                .map(c -> CategoryDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .slug(c.getSlug())
                        .parentCategoryId(c.getParentCategory() != null ? c.getParentCategory().getId() : null)
                        .parentCategoryName(c.getParentCategory() != null ? c.getParentCategory().getName() : null)
                        .build())
                .collect(Collectors.toSet());
    }

    private List<ProductVariantDto> mapVariants(List<ProductVariant> variants) {
        if (variants == null) return Collections.emptyList();
        return variants.stream()
                .map(v -> ProductVariantDto.builder()
                        .id(v.getId())
                        .sku(v.getSku())
                        .barcode(v.getBarcode())
                        .price(v.getPrice())
                        .effectivePrice(v.getEffectivePrice())
                        .stockQuantity(v.getStockQuantity())
                        .attributes(v.getAttributes())
                        .build())
                .toList();
    }
}