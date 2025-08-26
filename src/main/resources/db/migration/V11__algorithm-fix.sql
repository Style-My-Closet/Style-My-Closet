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

UPDATE clothing_conditions
    SET embedding =
            (ARRAY(
                    SELECT e / sqrt(embedding <#> embedding)
                    FROM unnest(embedding) AS e
             ))::vector
    WHERE TRUE;

CREATE OR REPLACE FUNCTION normalize_embedding()
    RETURNS trigger AS $$
BEGIN
    NEW.embedding :=
            (ARRAY(
                    SELECT e / sqrt(NEW.embedding <#> NEW.embedding)
                    FROM unnest(NEW.embedding) AS e
             ))::vector;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_normalize_embedding
    BEFORE INSERT OR UPDATE ON clothing_conditions
    FOR EACH ROW
EXECUTE FUNCTION normalize_embedding();


-- unique constraint 생성
ALTER TABLE weather
    ADD CONSTRAINT uq_weather_forecast_per_day UNIQUE (created_date, forecast_at, location_id);
