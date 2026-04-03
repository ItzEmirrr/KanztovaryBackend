package kg.kanztovary.kanztovarybackend.domain.controller;

import kg.kanztovary.kanztovarybackend.domain.dto.kpi.KpiResponse;
import kg.kanztovary.kanztovarybackend.domain.enums.KpiPeriod;
import kg.kanztovary.kanztovarybackend.domain.service.KpiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/kpi")
@PreAuthorize("hasRole('ADMIN')")
public class KpiController {

    private final KpiService kpiService;

    @GetMapping
    public KpiResponse getKpi(
            @RequestParam(defaultValue = "MONTH") KpiPeriod period) {
        log.info("KPI запрос, период={}", period);
        return kpiService.getKpi(period);
    }
}