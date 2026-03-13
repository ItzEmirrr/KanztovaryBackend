package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import kg.kanztovary.kanztovarybackend.domain.dto.product.*;
import kg.kanztovary.kanztovarybackend.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    /**
     * Каталог с поиском, фильтрацией и пагинацией.
     * Пользователь видит только ACTIVE. Администратор может фильтровать по любому статусу.
     */
    @GetMapping
    public ProductPageResponse search(@ModelAttribute ProductFilterRequest filter) {
        return productService.search(filter);
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    /**
     * Поиск по штрих-коду или SKU — для кассового сканера.
     * Порядок: штрих-код варианта → штрих-код товара → SKU варианта → SKU товара.
     * Ответ содержит полный товар + ID варианта (если код принадлежал варианту).
     */
    @GetMapping("/barcode/{code}")
    public BarcodeSearchResponse findByCode(@PathVariable String code) {
        return productService.findByCode(code);
    }


    /**
     * Создать товар с изображениями за один запрос.
     *
     * <pre>
     * POST /api/v1/products
     * Content-Type: multipart/form-data
     *
     * data:   {"name":"Ручка Parker","price":1500,"sku":"PEN-001","categoryIds":[1]}
     * images: photo1.jpg          ← главное изображение (isMain = true)
     * images: photo2.jpg
     * images: photo3.webp
     * </pre>
     *
     * Часть {@code data} должна иметь Content-Type: application/json.
     * Изображения (JPEG / PNG / WebP, не более 5 МБ каждое, не более 10 штук)
     * — необязательны, можно добавить позже через PUT.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto create(
            @RequestPart("data") @Valid ProductRequestDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        log.info("Создание товара: sku={}, изображений={}", request.getSku(),
                images == null ? 0 : images.size());
        return productService.create(request, images);
    }

    /**
     * Обновить товар.
     *
     * <p>Если передать новые файлы в {@code images} — старые изображения
     * будут удалены с диска и заменены новыми.<br>
     * Если {@code images} не передаётся — изображения остаются нетронутыми.
     *
     * <pre>
     * PUT /api/v1/products/42
     * Content-Type: multipart/form-data
     *
     * data:   {"name":"Ручка Parker Pro","price":1800,...}
     * images: new_photo.jpg       ← опционально; если есть — заменяет все старые
     * </pre>
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto update(
            @PathVariable Long id,
            @RequestPart("data") @Valid ProductRequestDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        log.info("Обновление товара id={}, новых изображений={}", id,
                images == null ? 0 : images.size());
        return productService.update(id, request, images);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDataDto delete(@PathVariable Long id) {
        log.info("Удаление товара id={}", id);
        return productService.delete(id);
    }
}