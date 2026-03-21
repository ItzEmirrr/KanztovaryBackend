package kg.kanztovary.kanztovarybackend.domain.dto.order;

import kg.kanztovary.kanztovarybackend.domain.dto.orderitem.OrderItemResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String username;
    private String status;

    /** Сумма товаров */
    private BigDecimal totalPrice;
    /** Стоимость доставки (0 при самовывозе или бесплатной доставке) */
    private BigDecimal deliveryFee;
    /** Итого к оплате = totalPrice + deliveryFee */
    private BigDecimal grandTotal;

    private String deliveryType;
    private String deliveryAddress;
    private String phoneNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryDto> statusHistory;
}
