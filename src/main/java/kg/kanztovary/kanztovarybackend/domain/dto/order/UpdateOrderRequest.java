package kg.kanztovary.kanztovarybackend.domain.dto.order;

import jakarta.validation.constraints.NotNull;
import kg.kanztovary.kanztovarybackend.domain.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderRequest {

    @NotNull(message = "Статус обязателен")
    private OrderStatus status;
}