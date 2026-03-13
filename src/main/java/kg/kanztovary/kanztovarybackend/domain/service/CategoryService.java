package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryDto;
import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryRequestDto;
import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    ResponseDataDto createCategory(CategoryRequestDto request);

    CategoryDto getById(Integer id);

    List<CategoryDto> getAllCategories();

    ResponseDataDto updateCategory(Integer id, CategoryRequestDto request);

    ResponseDataDto deleteCategory(Integer id);
}