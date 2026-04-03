package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.User;
import kg.kanztovary.kanztovarybackend.domain.dto.retail.*;

public interface RetailSaleService {

    RetailSaleResponse create(User admin, CreateRetailSaleRequest request);

    RetailSaleResponse getById(Long id);

    RetailSalePageResponse getAll(RetailSaleFilterRequest filter);

    /**
     * Удалить запись о продаже.
     * Склад автоматически восстанавливается.
     */
    void delete(Long id);

    RetailSaleSummaryDto getSummary(RetailSaleFilterRequest filter);
}