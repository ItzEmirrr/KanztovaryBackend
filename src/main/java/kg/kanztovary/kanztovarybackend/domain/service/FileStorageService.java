package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.domain.dto.productImage.ProductImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    /**
     * Сохранить один файл и вернуть URL.
     * Используется для одиночной загрузки через /api/v1/files/upload.
     */
    String saveFile(MultipartFile file);

    /**
     * Сохранить несколько изображений для товара.
     * Первый файл автоматически становится главным (isMain = true).
     * sortOrder выставляется по индексу в списке.
     * Формат файлов: JPEG, PNG или WebP — проверяется по сигнатуре байт.
     *
     * @param files    список загружаемых файлов
     * @param altText  alt-текст по умолчанию (обычно название товара)
     * @return список готовых DTO для сохранения в БД
     */
    List<ProductImageDto> saveProductImages(List<MultipartFile> files, String altText);

    /**
     * Удалить файл с диска по его URL (/uploads/filename.jpg).
     * Не бросает исключение если файл не найден.
     */
    void deleteFile(String fileUrl);
}