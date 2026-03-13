package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import kg.kanztovary.kanztovarybackend.domain.dto.product.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    /**
     * Создать товар вместе с изображениями в одной операции.
     * Изображения сохраняются на диск и сразу привязываются к товару.
     * Первый файл в списке автоматически становится главным изображением.
     */
    ProductDto create(ProductRequestDto request, List<MultipartFile> images);

    ProductDto getById(Long id);

    /**
     * Поиск и фильтрация каталога с пагинацией.
     */
    ProductPageResponse search(ProductFilterRequest filter);

    /**
     * Обновить товар.
     * Если {@code images} не пустой — старые изображения удаляются с диска
     * и заменяются новыми. Если список пуст — изображения не трогаются.
     */
    ProductDto update(Long id, ProductRequestDto request, List<MultipartFile> images);

    /** Выставляет статус DELETED */
    ResponseDataDto delete(Long id);

    /**
     * Поиск по штрих-коду или артикулу (SKU).
     * Порядок: штрих-код варианта → штрих-код товара → SKU варианта → SKU товара.
     * Возвращает товар и ID совпавшего варианта (если сканировали вариант).
     */
    BarcodeSearchResponse findByCode(String code);
}