CREATE TABLE ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    current_stock NUMERIC(12,2) NOT NULL DEFAULT 0,
    low_stock_threshold NUMERIC(12,2) NOT NULL DEFAULT 0
);