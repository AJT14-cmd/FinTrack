ALTER TABLE categories
    ADD COLUMN app_user_id BIGINT NOT NULL;

ALTER TABLE categories
    ADD CONSTRAINT fk_categories_user
        FOREIGN KEY (app_user_id)
            REFERENCES app_user(id);

CREATE INDEX idx_categories_app_user_id
    ON categories(app_user_id);