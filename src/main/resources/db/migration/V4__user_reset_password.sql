ALTER TABLE users
    ADD COLUMN temp_password VARCHAR(255),
    ADD COLUMN reset_password_time TIMESTAMP;