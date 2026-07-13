-- Добавляет поле customer_name в таблицу orders для гостевых заказов
ALTER TABLE stationery.orders
    ADD COLUMN IF NOT EXISTS customer_name VARCHAR(100);
