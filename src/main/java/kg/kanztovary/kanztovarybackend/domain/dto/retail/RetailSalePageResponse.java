package kg.kanztovary.kanztovarybackend.domain.dto.retail;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RetailSalePageResponse {

    private List<RetailSaleResponse> sales;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}