package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Category;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.CategoryRepository;
import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryDto;
import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryRequestDto;
import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.mapper.CategoryMapper;
import kg.kanztovary.kanztovarybackend.domain.service.CategoryService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public ResponseDataDto createCategory(CategoryRequestDto request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResponseException(
                    ResponseStatus.CATEGORY_EXISTS.getCode(),
                    ResponseStatus.CATEGORY_EXISTS.getMessage()
            );
        }

        Category category = categoryMapper.toEntity(request);

        if (request.getParentCategoryId() != null) {
            Category parent = findCategoryById(request.getParentCategoryId());
            category.setParentCategory(parent);
        }

        categoryRepository.save(category);
        log.info("Создана категория: {}", category.getName());

        return ResponseDataDto.builder()
                .code(ResponseStatus.SUCCESS.getCode())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Integer id) {
        return categoryMapper.toDto(findCategoryById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public ResponseDataDto updateCategory(Integer id, CategoryRequestDto request) {
        Category category = findCategoryById(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(request.getSlug());

        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId().equals(id)) {
                throw new ResponseException(ResponseStatus.BAD_REQUEST.getCode(),
                        "Категория не может быть своей же родительской");
            }
            Category parent = findCategoryById(request.getParentCategoryId());
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        categoryRepository.save(category);
        log.info("Обновлена категория: {}", category.getName());

        return ResponseDataDto.builder()
                .code(ResponseStatus.SUCCESS.getCode())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }

    @Override
    public ResponseDataDto deleteCategory(Integer id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
        log.info("Удалена категория: {}", category.getName());

        return ResponseDataDto.builder()
                .code(ResponseStatus.SUCCESS.getCode())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }

    private Category findCategoryById(Integer id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new ResponseException(
                        ResponseStatus.CATEGORY_NOT_FOUND.getCode(),
                        ResponseStatus.CATEGORY_NOT_FOUND.getMessage()
                )
        );
    }
}