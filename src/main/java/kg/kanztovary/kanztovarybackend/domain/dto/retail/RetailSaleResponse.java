package kg.kanztovary.kanztovarybackend.domain.dto.retail;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RetailSaleResponse {

    private Long id;
    private String adminUsername;
    private String note;
    private BigDecimal totalAmount;
    private List<RetailSaleItemResponse> items;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}