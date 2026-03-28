package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.*;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.ProductRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.ProductVariantRepository;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.RetailSaleRepository;
import kg.kanztovary.kanztovarybackend.domain.dto.retail.*;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.service.RetailSaleService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailSaleServiceImpl implements RetailSaleService {

    private final RetailSaleRepository retailSaleRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    @Override
    @Transactional
    public RetailSaleResponse create(User admin, CreateRetailSaleRequest request) {
        List<RetailSaleItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (RetailSaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResponseException(
                            ResponseStatus.PRODUCT_NOT_FOUND.getCode(),
                            "Товар не найден: id=" + itemReq.getProductId()
                    ));

            ProductVariant variant = null;

            if (itemReq.getVariantId() != null) {
                variant = variantRepository.findById(itemReq.getVariantId())
                        .orElseThrow(() -> new ResponseException(
                                ResponseStatus.PRODUCT_NOT_FOUND.getCode(),
                                "Вариация не найдена: id=" + itemReq.getVariantId()
                        ));

                if (variant.getStockQuantity() < itemReq.getQuantity()) {
                    throw new ResponseException(
                            ResponseStatus.NO_STOCK_SPACE.getCode(),
                            "Недостаточно «" + product.getName() +
                            " (" + formatAttributes(variant) + ")» на складе. " +
                            "Доступно: " + variant.getStockQuantity()
                    );
                }
                variant.setStockQuantity(variant.getStockQuantity() - itemReq.getQuantity());
                variantRepository.save(variant);
            } else {
                if (product.getStockQuantity() < itemReq.getQuantity()) {
                    throw new ResponseException(
                            ResponseStatus.NO_STOCK_SPACE.getCode(),
                            "Недостаточно товара «" + product.getName() + "» на складе. " +
                            "Доступно: " + product.getStockQuantity()
                    );
                }
                product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
                productRepository.save(product);
            }

            // Цена: вариация → скидка товара → цена товара
            BigDecimal price = variant != null
                    ? variant.getEffectivePrice()
                    : (product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice());

            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            items.add(RetailSaleItem.builder()
                    .product(product)
                    .variant(variant)
                    .quantity(itemReq.getQuantity())
                    .priceAtSale(price)
                    .subtotal(subtotal)
                    .build());
        }

        RetailSale sale = RetailSale.builder()
                .admin(admin)
                .note(request.getNote())
                .totalAmount(total)
                .items(items)
                .build();

        items.forEach(item -> item.setRetailSale(sale));

        retailSaleRepository.save(sale);
        log.info("Розничная продажа #{} на сумму {} — admin: {}", sale.getId(), total, admin.getEmail());

        return toResponse(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public RetailSaleResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public RetailSalePageResponse getAll(RetailSaleFilterRequest filter) {
        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize());
        Page<RetailSale> page;

        if (filter.getFrom() != null || filter.getTo() != null) {
            LocalDateTime from = filter.getFrom() != null
                    ? filter.getFrom().atStartOfDay()
                    : LocalDate.of(2000, 1, 1).atStartOfDay();
            LocalDateTime to = filter.getTo() != null
                    ? filter.getTo().atTime(LocalTime.MAX)
                    : LocalDateTime.now();
            page = retailSaleRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pageable);
        } else {
            page = retailSaleRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return RetailSalePageResponse.builder()
                .sales(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RetailSale sale = findById(id);
        for (RetailSaleItem item : sale.getItems()) {
            if (item.getVariant() != null) {
                ProductVariant v = item.getVariant();
                v.setStockQuantity(v.getStockQuantity() + item.getQuantity());
                variantRepository.save(v);
            } else {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
        retailSaleRepository.delete(sale);
        log.info("Розничная продажа #{} удалена, склад восстановлен", id);
    }


    /** "синий, A4" — для читаемого сообщения об ошибке остатка */
    private String formatAttributes(ProductVariant variant) {
        if (variant.getAttributes() == null || variant.getAttributes().isEmpty()) {
            return variant.getSku();
        }
        return String.join(", ", variant.getAttributes().values());
    }

    private RetailSale findById(Long id) {
        return retailSaleRepository.findById(id)
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.RETAIL_SALE_NOT_FOUND.getCode(),
                        ResponseStatus.RETAIL_SALE_NOT_FOUND.getMessage()
                ));
    }

    private RetailSaleResponse toResponse(RetailSale sale) {
        List<RetailSaleItemResponse> itemResponses = sale.getItems().stream()
                .map(item -> {
                    ProductVariant v = item.getVariant();
                    return RetailSaleItemResponse.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .sku(item.getProduct().getSku())
                            .barcode(item.getProduct().getBarcode())
                            .variantId(v != null ? v.getId() : null)
                            .variantSku(v != null ? v.getSku() : null)
                            .variantBarcode(v != null ? v.getBarcode() : null)
                            .variantAttributes(v != null ? v.getAttributes() : null)
                            .quantity(item.getQuantity())
                            .priceAtSale(item.getPriceAtSale())
                            .subtotal(item.getSubtotal())
                            .build();
                })
                .toList();

        return RetailSaleResponse.builder()
                .id(sale.getId())
                .adminUsername(sale.getAdmin().getUsername())
                .note(sale.getNote())
                .totalAmount(sale.getTotalAmount())
                .items(itemResponses)
                .createdAt(sale.getCreatedAt())
                .build();
    }
}