package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Order;

public interface TelegramService {

    void notifyNewOrder(Order order);
}
