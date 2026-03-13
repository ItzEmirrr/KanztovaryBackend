package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.domain.dto.productImage.ProductImageDto;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.service.FileStorageService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    // Проверяем первые байты файла — content-type легко подделать
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC  = {(byte) 0x89, 0x50, 0x4E, 0x47};
    // WebP: байты 0-3 = "RIFF", байты 8-11 = "WEBP"
    private static final byte[] WEBP_RIFF  = {0x52, 0x49, 0x46, 0x46};
    private static final byte[] WEBP_MARK  = {0x57, 0x45, 0x42, 0x50};

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size-bytes:5242880}")        // 5 MB по умолчанию
    private long maxSizeBytes;

    @Value("${file.max-images-per-product:10}")
    private int maxImagesPerProduct;

    // ─── Одиночная загрузка ───────────────────────────────────────────────────

    @Override
    public String saveFile(MultipartFile file) {
        validateImage(file);
        return storeFile(file);
    }

    // ─── Пакетная загрузка для товара ────────────────────────────────────────

    @Override
    public List<ProductImageDto> saveProductImages(List<MultipartFile> files, String altText) {
        if (files == null || files.isEmpty()) return List.of();

        if (files.size() > maxImagesPerProduct) {
            throw new ResponseException(
                    ResponseStatus.BAD_REQUEST.getCode(),
                    "Максимальное количество изображений — " + maxImagesPerProduct
            );
        }

        List<ProductImageDto> result = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            validateImage(file);
            String url = storeFile(file);

            result.add(ProductImageDto.builder()
                    .imageUrl(url)
                    .altText(altText)        // alt = название товара по умолчанию
                    .isMain(i == 0)          // первый файл — главный
                    .sortOrder(i)
                    .build());
        }

        log.info("Сохранено {} изображений", result.size());
        return result;
    }


    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
            log.debug("Файл удалён с диска: {}", filePath);
        } catch (IOException e) {
            log.warn("Не удалось удалить файл: {} — {}", fileUrl, e.getMessage());
        }
    }

    /**
     * Валидирует изображение: размер и формат по сигнатуре байт.
     * Бросает {@link ResponseException} при нарушении.
     */
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseException(ResponseStatus.BAD_REQUEST.getCode(),
                    "Получен пустой файл");
        }

        if (file.getSize() > maxSizeBytes) {
            long limitMb = maxSizeBytes / (1024 * 1024);
            throw new ResponseException(ResponseStatus.BAD_REQUEST.getCode(),
                    "Файл «" + file.getOriginalFilename() + "» превышает " + limitMb + " МБ");
        }

        if (!isAllowedImageFormat(file)) {
            throw new ResponseException(ResponseStatus.BAD_REQUEST.getCode(),
                    "Недопустимый формат файла «" + file.getOriginalFilename()
                    + "». Разрешены: JPEG, PNG, WebP");
        }
    }

    /**
     * Проверяет формат файла по сигнатуре первых байт (magic bytes),
     * а не по расширению или Content-Type — они могут быть подделаны.
     */
    private boolean isAllowedImageFormat(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = is.readNBytes(12);
            return isJpeg(header) || isPng(header) || isWebP(header);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isJpeg(byte[] header) {
        return header.length >= 3
                && header[0] == JPEG_MAGIC[0]
                && header[1] == JPEG_MAGIC[1]
                && header[2] == JPEG_MAGIC[2];
    }

    private boolean isPng(byte[] header) {
        return header.length >= 4
                && header[0] == PNG_MAGIC[0]
                && header[1] == PNG_MAGIC[1]
                && header[2] == PNG_MAGIC[2]
                && header[3] == PNG_MAGIC[3];
    }

    private boolean isWebP(byte[] header) {
        return header.length >= 12
                && header[0] == WEBP_RIFF[0]
                && header[1] == WEBP_RIFF[1]
                && header[2] == WEBP_RIFF[2]
                && header[3] == WEBP_RIFF[3]
                && header[8] == WEBP_MARK[0]
                && header[9] == WEBP_MARK[1]
                && header[10] == WEBP_MARK[2]
                && header[11] == WEBP_MARK[3];
    }

    /**
     * Сохраняет файл на диск, возвращает его URL.
     * Имя файла: uuid + оригинальное имя (очищенное от спецсимволов).
     */
    private String storeFile(MultipartFile file) {
        try {
            String originalName = sanitizeFilename(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + "_" + originalName;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(file.getInputStream(), uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + fileName;
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла: {}", e.getMessage());
            throw new ResponseException(ResponseStatus.INTERNAL_ERROR.getCode(),
                    "Не удалось сохранить файл. Попробуйте ещё раз.");
        }
    }

    private String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) return "image";
        return name.replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }
}