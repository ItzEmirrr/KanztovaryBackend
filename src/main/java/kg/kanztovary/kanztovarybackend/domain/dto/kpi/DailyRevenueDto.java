package kg.kanztovary.kanztovarybackend.domain.dto.kpi;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Одна точка на графике выручки */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyRevenueDto {
    private LocalDate day;
    private BigDecimal revenue;
    private Long orderCount;
}