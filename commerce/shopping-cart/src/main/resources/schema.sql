CREATE SCHEMA IF NOT EXISTS carts;

CREATE TABLE IF NOT EXISTS carts.shopping_carts (
    shopping_cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255),
    shopping_cart_state VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS carts.products_in_shopping_carts (
    shopping_cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (shopping_cart_id, product_id),
    FOREIGN KEY (shopping_cart_id) REFERENCES shopping_carts(shopping_cart_id) ON DELETE CASCADE
);