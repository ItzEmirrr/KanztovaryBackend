package kg.kanztovary.kanztovarybackend.domain.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kg.kanztovary.kanztovarybackend.domain.dto.orderitem.OrderItemDto;
import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GuestCheckoutRequest {

    @NotBlank(message = "Укажите ваше имя")
    private String customerName;

    @NotBlank(message = "Укажите номер телефона")
    private String phoneNumber;

    @NotNull(message = "Укажите способ получения заказа")
    private DeliveryType deliveryType;

    private String deliveryAddress;

    @NotEmpty(message = "Список товаров не может быть пустым")
    private List<OrderItemDto> items;
}
