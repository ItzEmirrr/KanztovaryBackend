package kg.kanztovary.kanztovarybackend.domain.dto.retail;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateRetailSaleRequest {

    @NotEmpty(message = "Список товаров не может быть пустым")
    @Valid
    private List<RetailSaleItemRequest> items;

    @Size(max = 500, message = "Заметка не должна превышать 500 символов")
    private String note;
}