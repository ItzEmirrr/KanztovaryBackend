package kg.kanztovary.kanztovarybackend.domain.enums;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum ResponseStatus {
    SUCCESS(0, "Успешно"),

    BAD_REQUEST(400, "Ошибка данных запроса"),
    NOT_FOUND(404, "Данные не найдены"),
    CATEGORY_NOT_FOUND(404, "Категория не найдена"),
    PRODUCT_NOT_FOUND(404, "Продукт не найден"),
    CUSTOMER_NOT_FOUND(404, "Клиент не существует"),

    CATEGORY_EXISTS(600, "Категория с таким названием уже существует"),
    SLUG_EXISTS(619, "Категория с таким slug уже существует"),
    NO_STOCK_SPACE(601, "Недостаточно товара на складе"),
    PHONE_EXISTS(602, "Клиент с таким номером уже существует"),
    ORDER_NOT_FOUND(603, "Заказ не найден"),
    EMAIL_EXISTS(604, "Пользователь с таким email уже существует"),
    USERNAME_EXISTS(605, "Пользователь с таким именем уже существует"),
    USER_NOT_FOUND(606, "Пользователь не найден"),
    INVALID_CREDENTIALS(607, "Неверный email или пароль"),
    SKU_EXISTS(608, "Товар с таким артикулом уже существует"),
    BRAND_NOT_FOUND(609, "Бренд не найден"),
    BRAND_EXISTS(610, "Бренд с таким названием уже существует"),
    CART_IS_EMPTY(611, "Корзина пуста"),
    CART_ITEM_NOT_FOUND(612, "Позиция корзины не найдена"),
    WRONG_PASSWORD(613, "Неверный текущий пароль"),
    PASSWORD_MISMATCH(614, "Пароли не совпадают"),
    REVIEW_NOT_FOUND(615, "Отзыв не найден"),
    ALREADY_REVIEWED(616, "Вы уже оставили отзыв"),
    REVIEW_ACCESS_DENIED(617, "Нет прав для изменения этого отзыва"),
    RETAIL_SALE_NOT_FOUND(618, "Розничная продажа не найдена"),
    INTERNAL_ERROR(500, "Внутренняя ошибка сервера");

    private final int code;
    private final String message;

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}