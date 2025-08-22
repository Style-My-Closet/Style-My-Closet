CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE weather
    ADD CONSTRAINT uq_weather_forecast UNIQUE (created_at, forecast_at, location_id);

ALTER TABLE clothing_conditions
    ADD COLUMN embedding VECTOR(28);  -- 28차원 feature 벡터