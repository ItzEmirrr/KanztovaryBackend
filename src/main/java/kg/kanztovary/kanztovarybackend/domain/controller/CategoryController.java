package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryDto;
import kg.kanztovary.kanztovarybackend.domain.dto.category.CategoryRequestDto;
import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import kg.kanztovary.kanztovarybackend.domain.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseDataDto create(@Valid @RequestBody CategoryRequestDto request) {
        log.info("Создание категории: {}", request);

        return categoryService.createCategory(request);
    }

    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable Integer id) {
        log.info("Получение категории с ID: {}", id);

        return categoryService.getById(id);
    }

    @GetMapping
    public List<CategoryDto> getAll() {
        return categoryService.getAllCategories();
    }

    @PutMapping("/{id}")
    public ResponseDataDto update(@PathVariable Integer id,
                                  @RequestBody CategoryRequestDto request) {
        log.info("Обновление категории с ID: {}, REQUEST: {}", id, request);

        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseDataDto delete(@PathVariable Integer id) {
        log.info("Удаление категории с ID: {}", id);

        return categoryService.deleteCategory(id);
    }

}