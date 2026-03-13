package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.*;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.*;
import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import kg.kanztovary.kanztovarybackend.domain.dto.product.*;
import kg.kanztovary.kanztovarybackend.domain.dto.productImage.ProductImageDto;
import kg.kanztovary.kanztovarybackend.domain.enums.ProductSortBy;
import kg.kanztovary.kanztovarybackend.domain.enums.ProductStatus;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.mapper.ProductImageMapper;
import kg.kanztovary.kanztovarybackend.domain.mapper.ProductMapper;
import kg.kanztovary.kanztovarybackend.domain.service.FileStorageService;
import kg.kanztovary.kanztovarybackend.domain.service.ProductService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final FileStorageService fileStorageService;
    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;

    @Override
    public ProductDto create(ProductRequestDto request, List<MultipartFile> images) {
        validateSku(request.getSku(), null);

        Brand brand = resolveBrand(request.getBrandId());
        Set<Category> categories = resolveCategories(request.getCategoryIds());

        Product product = productMapper.toEntity(request, categories, brand);
        productRepository.save(product);

        saveImages(product, images);
        saveVariants(product, request);

        log.info("Создан товар id={}, sku={}, изображений={}", product.getId(),
                product.getSku(), images == null ? 0 : images.size());
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        return productMapper.toResponse(findActiveById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponse search(ProductFilterRequest filter) {
        ProductStatus effectiveStatus = resolveStatus(filter);
        Specification<Product> spec = ProductSpecification.withFilters(filter, effectiveStatus);
        Sort sort = buildSort(filter.getSortBy());
        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Product> page = productRepository.findAll(spec, pageable);

        List<ProductDto> products = page.getContent()
                .stream()
                .map(productMapper::toResponse)
                .toList();

        log.info("Поиск товаров: '{}', статус={}, страница={}/{}, всего={}",
                filter.getSearch(), effectiveStatus, filter.getPage(),
                page.getTotalPages(), page.getTotalElements());

        return ProductPageResponse.builder()
                .products(products)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public ProductDto update(Long id, ProductRequestDto request, List<MultipartFile> images) {
        Product product = findActiveById(id);

        if (!product.getSku().equals(request.getSku())) {
            validateSku(request.getSku(), id);
        }

        Brand brand = resolveBrand(request.getBrandId());
        Set<Category> categories = resolveCategories(request.getCategoryIds());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setSku(request.getSku());
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getBarcode() != null && !request.getBarcode().equals(product.getBarcode())) {
            if (productRepository.existsByBarcodeAndIdNot(request.getBarcode(), id)) {
                throw new ResponseException(
                        ResponseStatus.BAD_REQUEST.getCode(),
                        "Товар с таким штрих-кодом уже существует: " + request.getBarcode()
                );
            }
        }
        product.setBarcode(request.getBarcode());
        product.setBrand(brand);
        product.setCategories(categories);

        boolean hasNewImages = images != null && !images.isEmpty();
        if (hasNewImages) {
            deleteExistingImages(product);
            saveImages(product, images);
        }

        productVariantRepository.deleteAllByProductId(product.getId());
        saveVariants(product, request);

        productRepository.save(product);

        log.info("Обновлён товар id={}, изображений заменено: {}", id, hasNewImages);
        return productMapper.toResponse(product);
    }

    @Override
    public ResponseDataDto delete(Long id) {
        Product product = findActiveById(id);
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
        log.info("Мягкое удаление товара id={}", id);

        return ResponseDataDto.builder()
                .code(ResponseStatus.SUCCESS.getCode())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }


    /**
     * Сохраняет изображения на диск и создаёт записи в БД.
     * Делегирует валидацию и запись на диск в {@link FileStorageService}.
     */
    private void saveImages(Product product, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        List<ProductImageDto> imageDtos =
                fileStorageService.saveProductImages(files, product.getName());

        List<ProductImage> entities = imageDtos.stream()
                .map(dto -> imageMapper.toEntity(product, dto))
                .toList();

        productImageRepository.saveAll(entities);
    }

    /**
     * Удаляет все изображения товара из БД и с диска.
     */
    private void deleteExistingImages(Product product) {
        List<ProductImage> existing = productImageRepository.findByProductId(product.getId());
        existing.forEach(img -> fileStorageService.deleteFile(img.getImageUrl()));
        productImageRepository.deleteByProductId(product.getId());
        log.debug("Удалено {} старых изображений товара id={}", existing.size(), product.getId());
    }


    private ProductStatus resolveStatus(ProductFilterRequest filter) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) {
            return filter.getStatus();
        }
        return ProductStatus.ACTIVE;
    }

    private Sort buildSort(ProductSortBy sortBy) {
        if (sortBy == null) return Sort.by("createdAt").descending();
        return switch (sortBy) {
            case PRICE_ASC  -> Sort.by("price").ascending();
            case PRICE_DESC -> Sort.by("price").descending();
            case NAME_ASC   -> Sort.by("name").ascending();
            case NAME_DESC  -> Sort.by("name").descending();
            case NEWEST     -> Sort.by("createdAt").descending();
            case OLDEST     -> Sort.by("createdAt").ascending();
        };
    }

    private Product findActiveById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.PRODUCT_NOT_FOUND.getCode(),
                        ResponseStatus.PRODUCT_NOT_FOUND.getMessage()
                ));
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new ResponseException(
                    ResponseStatus.PRODUCT_NOT_FOUND.getCode(),
                    ResponseStatus.PRODUCT_NOT_FOUND.getMessage()
            );
        }
        return product;
    }

    private void validateSku(String sku, Long excludeId) {
        productRepository.findBySku(sku).ifPresent(existing -> {
            if (!existing.getId().equals(excludeId)) {
                throw new ResponseException(
                        ResponseStatus.SKU_EXISTS.getCode(),
                        ResponseStatus.SKU_EXISTS.getMessage() + ": " + sku
                );
            }
        });
    }

    private Brand resolveBrand(Long brandId) {
        if (brandId == null) return null;
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.BRAND_NOT_FOUND.getCode(),
                        ResponseStatus.BRAND_NOT_FOUND.getMessage()
                ));
    }

    private Set<Category> resolveCategories(Set<Integer> categoryIds) {
        Set<Category> categories = new HashSet<>();
        for (Integer categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseException(
                            ResponseStatus.CATEGORY_NOT_FOUND.getCode(),
                            ResponseStatus.CATEGORY_NOT_FOUND.getMessage()
                    ));
            categories.add(category);
        }
        return categories;
    }

    @Override
    @Transactional(readOnly = true)
    public BarcodeSearchResponse findByCode(String code) {
        // 1. Штрих-код вариации
        Optional<ProductVariant> variantOpt = productVariantRepository.findByBarcode(code)
                .or(() -> productVariantRepository.findBySku(code));    // 3. SKU вариации

        if (variantOpt.isPresent()) {
            ProductVariant variant = variantOpt.get();
            Product product = variant.getProduct();
            checkNotDeleted(product);
            return BarcodeSearchResponse.builder()
                    .product(productMapper.toResponse(product))
                    .matchedVariantId(variant.getId())
                    .build();
        }

        // 2. Штрих-код товара → 4. SKU товара
        Product product = productRepository.findByBarcode(code)
                .or(() -> productRepository.findBySku(code))
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.PRODUCT_NOT_FOUND.getCode(),
                        ResponseStatus.PRODUCT_NOT_FOUND.getMessage()
                ));
        checkNotDeleted(product);
        return BarcodeSearchResponse.builder()
                .product(productMapper.toResponse(product))
                .matchedVariantId(null)
                .build();
    }

    private void checkNotDeleted(Product product) {
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new ResponseException(
                    ResponseStatus.PRODUCT_NOT_FOUND.getCode(),
                    ResponseStatus.PRODUCT_NOT_FOUND.getMessage()
            );
        }
    }

    private void saveVariants(Product product, ProductRequestDto request) {
        if (request.getVariants() == null || request.getVariants().isEmpty()) return;

        for (ProductVariantRequestDto dto : request.getVariants()) {
            if (productVariantRepository.existsBySku(dto.getSku())) {
                throw new ResponseException(
                        ResponseStatus.SKU_EXISTS.getCode(),
                        "Артикул вариации уже существует: " + dto.getSku()
                );
            }
        }

        List<ProductVariant> variants = request.getVariants()
                .stream()
                .map(dto -> ProductVariant.builder()
                        .product(product)
                        .sku(dto.getSku())
                        .barcode(dto.getBarcode())
                        .price(dto.getPrice())
                        .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0L)
                        .attributes(dto.getAttributes() != null
                                ? dto.getAttributes()
                                : new java.util.HashMap<>())
                        .build())
                .toList();

        productVariantRepository.saveAll(variants);
    }
}