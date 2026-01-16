CREATE SCHEMA IF NOT EXISTS shopping_store;

CREATE TABLE IF NOT EXISTS shopping_store.products (
    product_id UUID NOT NULL DEFAULT gen_random_uuid(),
    product_name VARCHAR(255),
    description TEXT NOT NULL,
    image_src VARCHAR(500),
    quantity_state VARCHAR(20)
        CHECK (quantity_state IN ('ENDED', 'FEW', 'ENOUGH', 'MANY')),
    product_state VARCHAR(20)
        CHECK (product_state IN ('ACTIVE', 'DEACTIVE')),
    product_category VARCHAR(50)
        CHECK (product_category IN ('LIGHTING', 'CONTROL', 'SENSORS')),
    price DECIMAL(10, 2),

    CONSTRAINT pk_products PRIMARY KEY (product_id),
    CONSTRAINT chk_price_positive CHECK (price >= 0)
);