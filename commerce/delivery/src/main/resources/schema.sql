CREATE SCHEMA IF NOT EXISTS deliveries;

CREATE TABLE IF NOT EXISTS deliveries.addresses (
    address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country VARCHAR(255),
    city VARCHAR(255),
    street VARCHAR(255),
    house VARCHAR(255),
    flat VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS deliveries.deliveries (
    delivery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    total_weight DOUBLE PRECISION NOT NULL,
    total_volume DOUBLE PRECISION NOT NULL,
    fragile BOOLEAN NOT NULL DEFAULT false,
    delivery_state VARCHAR(20) NOT NULL,
    from_address_id UUID REFERENCES deliveries.addresses(address_id) ON DELETE CASCADE,
    to_address_id UUID REFERENCES deliveries.addresses(address_id) ON DELETE CASCADE
);