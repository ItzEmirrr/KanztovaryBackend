package kg.kanztovary.kanztovarybackend.domain.mapper;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Category;
import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryDto;
import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryRequestDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequestDto request) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .build();
    }

    public CategoryDto toDto(Category entity) {
        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .slug(entity.getSlug())
                .parentCategoryId(entity.getParentCategory() != null ? entity.getParentCategory().getId() : null)
                .parentCategoryName(entity.getParentCategory() != null ? entity.getParentCategory().getName() : null)
                .build();
    }
}