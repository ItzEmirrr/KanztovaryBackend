package kg.kanztovary.kanztovarybackend.domain.mapper;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Brand;
import kg.kanztovary.kanztovarybackend.domain.dto.brand.BrandDto;
import kg.kanztovary.kanztovarybackend.domain.dto.brand.BrandRequestDto;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {

    public BrandDto toDto(Brand brand) {
        if (brand == null) return null;
        return BrandDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .websiteUrl(brand.getWebsiteUrl())
                .build();
    }

    public Brand toEntity(BrandRequestDto request) {
        return Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .websiteUrl(request.getWebsiteUrl())
                .build();
    }

    public void updateEntity(Brand brand, BrandRequestDto request) {
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setWebsiteUrl(request.getWebsiteUrl());
    }
}
