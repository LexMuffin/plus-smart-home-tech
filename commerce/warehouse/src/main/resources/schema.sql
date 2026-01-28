CREATE SCHEMA IF NOT EXISTS warehouse;

CREATE TABLE IF NOT EXISTS warehouse.products (
    product_id UUID PRIMARY KEY,
    fragile BOOLEAN NOT NULL,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    depth DOUBLE PRECISION,
    weight DOUBLE PRECISION NOT NULL,
    quantity BIGINT
);

CREATE TABLE IF NOT EXISTS warehouse.booking (
    booking_id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    delivery_id UUID,
    total_weight DOUBLE PRECISION NOT NULL,
    total_volume DOUBLE PRECISION NOT NULL,
    fragile BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS warehouse.booking_products (
    order_id UUID NOT NULL REFERENCES warehouse.booking(order_id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES warehouse.products(product_id) ON DELETE CASCADE,
    quantity BIGINT NOT NULL
);