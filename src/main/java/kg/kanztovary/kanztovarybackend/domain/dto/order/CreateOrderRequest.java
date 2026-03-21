package kg.kanztovary.kanztovarybackend.domain.dto.order;

import jakarta.validation.constraints.NotNull;
import kg.kanztovary.kanztovarybackend.domain.dto.orderitem.OrderItemDto;
import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CreateOrderRequest {

    private List<OrderItemDto> items;

    @NotNull(message = "Укажите способ получения заказа")
    private DeliveryType deliveryType;

    /** Адрес доставки — обязателен при deliveryType = DELIVERY */
    private String deliveryAddress;

    /** Телефон для связи с клиентом (обязателен всегда) */
    private String phoneNumber;
}