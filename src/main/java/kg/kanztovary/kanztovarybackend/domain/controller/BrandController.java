package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.domain.dto.brand.BrandDto;
import kg.kanztovary.kanztovarybackend.domain.dto.brand.BrandRequestDto;
import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import kg.kanztovary.kanztovarybackend.domain.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BrandDto create(@Valid @RequestBody BrandRequestDto request) {
        log.info("Создание бренда: {}", request.getName());
        return brandService.create(request);
    }

    @GetMapping("/{id}")
    public BrandDto getById(@PathVariable Long id) {
        return brandService.getById(id);
    }

    @GetMapping
    public List<BrandDto> getAll() {
        return brandService.getAll();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BrandDto update(@PathVariable Long id, @Valid @RequestBody BrandRequestDto request) {
        log.info("Обновление бренда id={}", id);
        return brandService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDataDto delete(@PathVariable Long id) {
        log.info("Удаление бренда id={}", id);
        return brandService.delete(id);
    }
}