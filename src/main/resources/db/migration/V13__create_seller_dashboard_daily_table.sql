CREATE TABLE seller_dashboard_daily (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    paid_order_count BIGINT NOT NULL,
    sales_quantity BIGINT NOT NULL,
    sales_amount DECIMAL(18,2) NOT NULL,
    buyer_count BIGINT NOT NULL,
    version BIGINT NULL,
    created_at DATETIME(6) NULL,
    modified_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_seller_dashboard_daily_seller_date UNIQUE (seller_id, stat_date)
);

CREATE INDEX idx_seller_dashboard_daily_seller_date
    ON seller_dashboard_daily (seller_id, stat_date);
