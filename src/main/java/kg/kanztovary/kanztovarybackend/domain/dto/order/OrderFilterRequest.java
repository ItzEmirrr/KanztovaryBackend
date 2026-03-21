package kg.kanztovary.kanztovarybackend.domain.dto.order;

import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import kg.kanztovary.kanztovarybackend.domain.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Параметры фильтрации и пагинации списка заказов.
 * Передаются как query-параметры: ?status=NEW&from=2024-01-01&page=0&size=10
 */
@Getter
@Setter
public class OrderFilterRequest {

    private OrderStatus status;

    private DeliveryType deliveryType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private Integer userId;

    private int page = 0;

    private int size = 10;
}