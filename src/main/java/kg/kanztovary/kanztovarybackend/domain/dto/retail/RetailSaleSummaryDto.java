package kg.kanztovary.kanztovarybackend.domain.dto.retail;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class RetailSaleSummaryDto {

    private long totalSales;
    private BigDecimal totalRevenue;
    private BigDecimal averageReceipt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;
}