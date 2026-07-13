package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Order;
import kg.kanztovary.kanztovarybackend.domain.service.TelegramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TelegramServiceImpl implements TelegramService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void notifyNewOrder(Order order) {
        if (botToken.isBlank() || chatId.isBlank()) {
            log.warn("Telegram не настроен: задайте telegram.bot.token и telegram.chat.id");
            return;
        }
        try {
            String message = buildMessage(order);
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            Map<String, String> body = Map.of(
                    "chat_id", chatId,
                    "text", message,
                    "parse_mode", "HTML"
            );
            restTemplate.postForObject(url, body, String.class);
            log.info("Telegram уведомление для заказа #{} отправлено", order.getId());
        } catch (Exception e) {
            log.error("Ошибка отправки Telegram уведомления для заказа #{}: {}", order.getId(), e.getMessage());
        }
    }

    private String buildMessage(Order order) {
        String clientName;
        if (order.getCustomerName() != null && !order.getCustomerName().isBlank()) {
            clientName = order.getCustomerName() + " (гость)";
        } else if (order.getUser() != null) {
            clientName = order.getUser().getUsername();
        } else {
            clientName = "Неизвестен";
        }

        String items = order.getItems().stream()
                .map(i -> "  • " + i.getProduct().getName()
                        + " ×" + i.getQuantity()
                        + " = " + i.getPrice() + " сом")
                .collect(Collectors.joining("\n"));

        String delivery = order.getDeliveryType().name().equals("PICKUP")
                ? "Самовывоз"
                : "Доставка: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "—");

        return String.format(
                "🛍 <b>Новый заказ #%d</b>\n" +
                "👤 Клиент: %s\n" +
                "📞 Телефон: %s\n" +
                "📦 %s\n" +
                "Товары:\n%s\n" +
                "💰 Итого: <b>%s сом</b>",
                order.getId(),
                clientName,
                order.getPhoneNumber(),
                delivery,
                items,
                order.getGrandTotal()
        );
    }
}
