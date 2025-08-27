ALTER TABLE weather
    DROP CONSTRAINT uq_weather_forecast;

ALTER TABLE weather
    ADD COLUMN created_date date;

UPDATE weather
    SET created_date = created_at::date
    WHERE created_date IS NULL;

CREATE OR REPLACE FUNCTION set_weather_dates()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.created_date := NEW.created_at::date;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trigger_set_dates
    BEFORE INSERT OR UPDATE ON weather
    FOR EACH ROW
EXECUTE FUNCTION set_created_date();


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

ALTER TABLE clothing_conditions DROP CONSTRAINT clothing_conditions_color_check;

ALTER TABLE clothing_conditions
    ADD CONSTRAINT clothing_conditions_color_check
        CHECK (color >= 0 AND color <= 10);