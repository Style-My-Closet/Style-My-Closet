ALTER TABLE users
    ADD COLUMN password_reset_token VARCHAR(255),
    ADD COLUMN reset_password_time TIMESTAMP,
    ADD COLUMN change_password_flag BOOLEAN NOT NULL DEFAULT false;;