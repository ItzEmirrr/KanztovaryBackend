package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.User;
import kg.kanztovary.kanztovarybackend.domain.dto.retail.*;
import kg.kanztovary.kanztovarybackend.domain.service.RetailSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/retail-sales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RetailSaleController {

    private final RetailSaleService retailSaleService;

    /**
     * Провести розничную продажу.
     * Штрих-код уже отсканирован на клиенте — сюда приходит список {productId, quantity}.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RetailSaleResponse create(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody CreateRetailSaleRequest request
    ) {
        return retailSaleService.create(admin, request);
    }

    /** История розничных продаж с фильтрацией по дате */
    @GetMapping
    public RetailSalePageResponse getAll(@ModelAttribute RetailSaleFilterRequest filter) {
        return retailSaleService.getAll(filter);
    }

    @GetMapping("/{id}")
    public RetailSaleResponse getById(@PathVariable Long id) {
        return retailSaleService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        retailSaleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Сводная статистика за период. */
    @GetMapping("/summary")
    public RetailSaleSummaryDto getSummary(@ModelAttribute RetailSaleFilterRequest filter) {
        return retailSaleService.getSummary(filter);
    }
}