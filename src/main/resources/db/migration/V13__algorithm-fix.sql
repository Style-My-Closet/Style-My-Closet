ALTER TABLE weather
    DROP CONSTRAINT uq_weather_forecast;

ALTER TABLE weather
    ADD COLUMN created_date date;

UPDATE weather
    SET created_date = created_at::date
    WHERE created_date IS NULL;

-- unique constraint 생성
ALTER TABLE weather
    ADD CONSTRAINT uq_weather_forecast_per_day UNIQUE (created_date, forecast_at, location_id);


ALTER TABLE clothing_conditions
    DROP COLUMN sleeve_length;
ALTER TABLE clothing_conditions
    DROP COLUMN pants_length;
ALTER TABLE clothing_conditions
    ADD COLUMN length SMALLINT;
ALTER TABLE clothing_conditions
    ADD COLUMN material SMALLINT;
ALTER TABLE clothing_conditions
    DROP COLUMN embedding;
ALTER TABLE clothing_conditions
    ADD COLUMN embedding VECTOR(44);

