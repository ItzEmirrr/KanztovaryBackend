package kg.kanztovary.kanztovarybackend.domain.dto.order;

import lombok.*;

import java.util.List;

/** Обёртка с пагинацией для списка заказов */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPageResponse {
    private List<OrderResponse> orders;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}