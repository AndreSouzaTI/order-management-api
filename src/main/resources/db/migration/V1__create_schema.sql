CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE products (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(120)   NOT NULL,
    description    VARCHAR(500),
    price          NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    stock_quantity INTEGER        NOT NULL CHECK (stock_quantity >= 0),
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    version        BIGINT         NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ    NOT NULL
);

CREATE TABLE orders (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT         NOT NULL REFERENCES users (id),
    status      VARCHAR(20)    NOT NULL,
    total       NUMERIC(12, 2) NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL,
    updated_at  TIMESTAMPTZ    NOT NULL
);

CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id BIGINT         NOT NULL REFERENCES products (id),
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_products_name ON products (LOWER(name));
