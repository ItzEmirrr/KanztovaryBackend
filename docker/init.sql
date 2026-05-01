CREATE SCHEMA IF NOT EXISTS stationery;

-- Расширения
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =============================================================================
-- 1. ПОЛЬЗОВАТЕЛИ
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.users (
                                                id         SERIAL      PRIMARY KEY,
                                                username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL DEFAULT 'USER'
    );

CREATE INDEX IF NOT EXISTS idx_users_email    ON stationery.users (email);
CREATE INDEX IF NOT EXISTS idx_users_username ON stationery.users (username);

-- =============================================================================
-- 2. БРЕНДЫ
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.brands (
                                                 id          BIGSERIAL    PRIMARY KEY,
                                                 name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    logo_url    VARCHAR(255),
    website_url VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_brands_name ON stationery.brands (name);

-- =============================================================================
-- 3. КАТЕГОРИИ (дерево)
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.categories (
                                                     id          SERIAL       PRIMARY KEY,
                                                     name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    slug        VARCHAR(100) UNIQUE,
    parent_id   INT          REFERENCES stationery.categories (id) ON DELETE SET NULL
    );

CREATE INDEX IF NOT EXISTS idx_categories_slug      ON stationery.categories (slug);
CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON stationery.categories (parent_id);

-- =============================================================================
-- 4. ТОВАРЫ
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.products (
                                                   id             BIGSERIAL        PRIMARY KEY,
                                                   name           VARCHAR(255)     NOT NULL,
    description    TEXT,
    price          NUMERIC(15, 2)   NOT NULL CHECK (price > 0),
    discount_price NUMERIC(15, 2)   CHECK (discount_price >= 0),
    sku            VARCHAR(100)     NOT NULL UNIQUE,
    barcode        VARCHAR(100)     UNIQUE,
    stock_quantity BIGINT           NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    status         VARCHAR(50)      NOT NULL DEFAULT 'ACTIVE',
    brand_id       BIGINT           REFERENCES stationery.brands (id) ON DELETE SET NULL,
    created_at     TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP        NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_products_sku        ON stationery.products (sku);
CREATE INDEX IF NOT EXISTS idx_products_barcode    ON stationery.products (barcode);
CREATE INDEX IF NOT EXISTS idx_products_brand_id   ON stationery.products (brand_id);
CREATE INDEX IF NOT EXISTS idx_products_status     ON stationery.products (status);
CREATE INDEX IF NOT EXISTS idx_products_price      ON stationery.products (price);
-- Trigram-индекс для быстрого поиска по названию (LIKE/ILIKE)
CREATE INDEX IF NOT EXISTS idx_products_name_trgm  ON stationery.products USING GIN (name gin_trgm_ops);

-- =============================================================================
-- 5. СВЯЗЬ ТОВАР ↔ КАТЕГОРИЯ (ManyToMany)
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.product_categories (
                                                             product_id  BIGINT NOT NULL REFERENCES stationery.products    (id) ON DELETE CASCADE,
    category_id INT    NOT NULL REFERENCES stationery.categories  (id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, category_id)
    );

CREATE INDEX IF NOT EXISTS idx_product_categories_category_id ON stationery.product_categories (category_id);

-- =============================================================================
-- 6. ВАРИАЦИИ ТОВАРА
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.product_variants (
                                                           id             BIGSERIAL      PRIMARY KEY,
                                                           product_id     BIGINT         NOT NULL REFERENCES stationery.products (id) ON DELETE CASCADE,
    sku            VARCHAR(100)   NOT NULL UNIQUE,
    barcode        VARCHAR(100)   UNIQUE,
    price          NUMERIC(15, 2) CHECK (price >= 0),
    stock_quantity BIGINT         NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP      NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON stationery.product_variants (product_id);
CREATE INDEX IF NOT EXISTS idx_product_variants_sku        ON stationery.product_variants (sku);
CREATE INDEX IF NOT EXISTS idx_product_variants_barcode    ON stationery.product_variants (barcode);

-- =============================================================================
-- 7. АТРИБУТЫ ВАРИАЦИИ (ElementCollection: key → value)
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.product_variant_attributes (
                                                                     variant_id      BIGINT       NOT NULL REFERENCES stationery.product_variants (id) ON DELETE CASCADE,
    attribute_key   VARCHAR(100) NOT NULL,
    attribute_value VARCHAR(255),
    PRIMARY KEY (variant_id, attribute_key)
    );

CREATE INDEX IF NOT EXISTS idx_variant_attributes_variant_id ON stationery.product_variant_attributes (variant_id);

-- =============================================================================
-- 8. ИЗОБРАЖЕНИЯ ТОВАРА
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.product_images (
                                                         id         BIGSERIAL    PRIMARY KEY,
                                                         product_id BIGINT       NOT NULL REFERENCES stationery.products (id) ON DELETE CASCADE,
    image_url  VARCHAR(255) NOT NULL,
    alt_text   VARCHAR(255),
    is_main    BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order INT          NOT NULL DEFAULT 0
    );

CREATE INDEX IF NOT EXISTS idx_product_images_product_id ON stationery.product_images (product_id);
CREATE INDEX IF NOT EXISTS idx_product_images_is_main    ON stationery.product_images (product_id, is_main);

-- =============================================================================
-- 9. КОРЗИНЫ
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.carts (
                                                id         BIGSERIAL   PRIMARY KEY,
                                                user_id    INT         NOT NULL REFERENCES stationery.users (id) ON DELETE CASCADE,
    status     VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW()
    );

-- Один пользователь — одна активная корзина
CREATE UNIQUE INDEX IF NOT EXISTS uq_carts_user_active
    ON stationery.carts (user_id)
    WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_carts_user_id ON stationery.carts (user_id);
CREATE INDEX IF NOT EXISTS idx_carts_status  ON stationery.carts (status);

-- =============================================================================
-- 10. ПОЗИЦИИ КОРЗИНЫ
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.cart_items (
                                                     id         BIGSERIAL NOT NULL PRIMARY KEY,
                                                     cart_id    BIGINT    NOT NULL REFERENCES stationery.carts            (id) ON DELETE CASCADE,
    product_id BIGINT    NOT NULL REFERENCES stationery.products         (id) ON DELETE CASCADE,
    variant_id BIGINT             REFERENCES stationery.product_variants (id) ON DELETE CASCADE,
    quantity   INT       NOT NULL CHECK (quantity > 0),
    -- Один и тот же товар+вариация не может быть в корзине дважды
    UNIQUE (cart_id, product_id, variant_id)
    );

CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id    ON stationery.cart_items (cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON stationery.cart_items (product_id);

-- =============================================================================
-- 11. ЗАКАЗЫ
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.orders (
                                                 id               BIGSERIAL      PRIMARY KEY,
                                                 user_id          INT            REFERENCES stationery.users (id) ON DELETE SET NULL,
    status           VARCHAR(50)    NOT NULL,
    total_price      NUMERIC(15, 2) NOT NULL CHECK (total_price >= 0),
    delivery_fee     NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (delivery_fee >= 0),
    delivery_type    VARCHAR(20)    NOT NULL,
    delivery_address VARCHAR(500),
    phone_number     VARCHAR(20)    NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_orders_user_id    ON stationery.orders (user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status     ON stationery.orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON stationery.orders (created_at DESC);

-- =============================================================================
-- 12. ПОЗИЦИИ ЗАКАЗА
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.order_items (
                                                      id         BIGSERIAL      PRIMARY KEY,
                                                      order_id   BIGINT         NOT NULL REFERENCES stationery.orders   (id) ON DELETE CASCADE,
    product_id BIGINT         NOT NULL REFERENCES stationery.products  (id) ON DELETE RESTRICT,
    quantity   INT            NOT NULL DEFAULT 1 CHECK (quantity > 0),
    price      NUMERIC(15, 2) NOT NULL CHECK (price >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_order_items_order_id   ON stationery.order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON stationery.order_items (product_id);

-- =============================================================================
-- 13. ИСТОРИЯ СТАТУСОВ ЗАКАЗА
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.order_status_history (
                                                               id              BIGSERIAL   PRIMARY KEY,
                                                               order_id        BIGINT      NOT NULL REFERENCES stationery.orders (id) ON DELETE CASCADE,
    previous_status VARCHAR(50),
    new_status      VARCHAR(50) NOT NULL,
    changed_by      VARCHAR(100) NOT NULL,
    changed_at      TIMESTAMP   NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id   ON stationery.order_status_history (order_id);
CREATE INDEX IF NOT EXISTS idx_order_status_history_changed_at ON stationery.order_status_history (changed_at DESC);

-- =============================================================================
-- 14. ОТЗЫВЫ О МАГАЗИНЕ
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.reviews (
                                                  id         BIGSERIAL    PRIMARY KEY,
                                                  user_id    INT          NOT NULL REFERENCES stationery.users (id) ON DELETE CASCADE,
    rating     SMALLINT     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    VARCHAR(1000),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    -- Один пользователь — один отзыв
    UNIQUE (user_id)
    );

CREATE INDEX IF NOT EXISTS idx_reviews_user_id  ON stationery.reviews (user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_rating   ON stationery.reviews (rating);
CREATE INDEX IF NOT EXISTS idx_reviews_created_at ON stationery.reviews (created_at DESC);

-- =============================================================================
-- 15. РОЗНИЧНЫЕ ПРОДАЖИ (касса)
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.retail_sales (
                                                       id           BIGSERIAL      PRIMARY KEY,
                                                       admin_id     INT            NOT NULL REFERENCES stationery.users (id) ON DELETE RESTRICT,
    note         VARCHAR(500),
    total_amount NUMERIC(15, 2) NOT NULL CHECK (total_amount >= 0),
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_retail_sales_admin_id   ON stationery.retail_sales (admin_id);
CREATE INDEX IF NOT EXISTS idx_retail_sales_created_at ON stationery.retail_sales (created_at DESC);

-- =============================================================================
-- 16. ПОЗИЦИИ РОЗНИЧНОЙ ПРОДАЖИ
-- =============================================================================
CREATE TABLE IF NOT EXISTS stationery.retail_sale_items (
                                                            id             BIGSERIAL      PRIMARY KEY,
                                                            retail_sale_id BIGINT         NOT NULL REFERENCES stationery.retail_sales     (id) ON DELETE CASCADE,
    product_id     BIGINT         NOT NULL REFERENCES stationery.products          (id) ON DELETE RESTRICT,
    -- При удалении вариации обнуляется (история продаж сохраняется)
    variant_id     BIGINT                  REFERENCES stationery.product_variants  (id) ON DELETE SET NULL,
    quantity       BIGINT         NOT NULL CHECK (quantity > 0),
    price_at_sale  NUMERIC(15, 2) NOT NULL CHECK (price_at_sale >= 0),
    subtotal       NUMERIC(15, 2) NOT NULL CHECK (subtotal >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_retail_sale_items_retail_sale_id ON stationery.retail_sale_items (retail_sale_id);
CREATE INDEX IF NOT EXISTS idx_retail_sale_items_product_id     ON stationery.retail_sale_items (product_id);
CREATE INDEX IF NOT EXISTS idx_retail_sale_items_variant_id     ON stationery.retail_sale_items (variant_id);
