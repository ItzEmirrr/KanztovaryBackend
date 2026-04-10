package kg.kanztovary.kanztovarybackend.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Текущий пароль обязателен")
    private String currentPassword;

    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
    private String newPassword;

    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;
}