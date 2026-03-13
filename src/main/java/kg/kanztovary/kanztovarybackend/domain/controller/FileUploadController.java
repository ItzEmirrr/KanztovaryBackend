package kg.kanztovary.kanztovarybackend.domain.controller;

import kg.kanztovary.kanztovarybackend.domain.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FileUploadController {

    private final FileStorageService storageService;

    /**
     * Загрузить одно изображение.
     * Этот эндпоинт используется для загрузки логотипов брендов и прочих
     * одиночных изображений. Для изображений товара предпочтительнее
     * передавать файлы прямо в POST/PUT /api/v1/products.
     *
     * <pre>
     * POST /api/v1/files/upload
     * Content-Type: multipart/form-data
     * file: image.jpg
     *
     * Response: { "url": "/uploads/uuid_image.jpg" }
     * </pre>
     */
    @PostMapping("/upload")
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Загрузка файла: {}, размер: {} байт",
                file.getOriginalFilename(), file.getSize());
        String url = storageService.saveFile(file);
        return Map.of("url", url);
    }

    /**
     * Загрузить несколько изображений за один запрос.
     * Возвращает список URL в том же порядке, в котором пришли файлы.
     *
     * <pre>
     * POST /api/v1/files/upload/batch
     * Content-Type: multipart/form-data
     * files: image1.jpg
     * files: image2.png
     * files: image3.webp
     *
     * Response: { "urls": ["/uploads/...", "/uploads/...", "/uploads/..."] }
     * </pre>
     */
    @PostMapping("/upload/batch")
    public Map<String, List<String>> uploadBatch(
            @RequestParam("files") List<MultipartFile> files) {
        log.info("Пакетная загрузка {} файлов", files.size());
        List<String> urls = files.stream()
                .map(storageService::saveFile)
                .toList();
        return Map.of("urls", urls);
    }
}