package kg.kanztovary.kanztovarybackend.domain.dto.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import lombok.Getter;
import lombok.Setter;

/**
 * Запрос на оформление заказа из корзины.
 *
 * <pre>
 * POST /api/v1/cart/checkout
 * {
 *   "deliveryType": "DELIVERY",
 *   "deliveryAddress": "г. Бишкек, ул. Манаса 1",
 *   "phoneNumber": "+996700123456"
 * }
 * </pre>
 *
 * При {@code deliveryType = PICKUP} поле {@code deliveryAddress} игнорируется.
 * Поле {@code phoneNumber} обязательно всегда — администратор связывается
 * с клиентом для подтверждения независимо от способа получения.
 */
@Getter
@Setter
public class CheckoutRequest {

    @NotNull(message = "Укажите способ получения заказа: PICKUP или DELIVERY")
    private DeliveryType deliveryType;

    @Size(max = 500, message = "Адрес доставки не должен превышать 500 символов")
    private String deliveryAddress;

    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(
            regexp = "^\\+?[0-9]{7,15}$",
            message = "Введите корректный номер телефона"
    )
    private String phoneNumber;
}