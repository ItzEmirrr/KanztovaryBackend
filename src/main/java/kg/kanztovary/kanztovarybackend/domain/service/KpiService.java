package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.domain.dto.kpi.KpiResponse;
import kg.kanztovary.kanztovarybackend.domain.enums.KpiPeriod;

public interface KpiService {
    /**
     * Возвращает полный набор KPI за указанный период.
     * Предыдущий аналогичный период рассчитывается автоматически для сравнения.
     */
    KpiResponse getKpi(KpiPeriod period);
}