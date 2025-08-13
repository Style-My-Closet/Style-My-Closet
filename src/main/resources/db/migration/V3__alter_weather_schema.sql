-- 1. alert_type ENUM 값 확장
ALTER TABLE weather
    DROP CONSTRAINT weather_alert_type_check;

ALTER TABLE weather
    ADD CONSTRAINT weather_alert_type_check CHECK (alert_type IN
                                                   ('NONE', 'RAIN', 'HEAVY_RAIN', 'SNOW_RAIN', 'SNOW', 'SHOWER', 'HIGH_TEMP', 'LOW_TEMP', 'STRONG_WIND'));

-- 2. precipitation 관련 컬럼 추가/이름 변경/정비
ALTER TABLE weather
    ALTER COLUMN alert_type TYPE VARCHAR(20);

-- 7. 기타 nullable 보장
ALTER TABLE weather
    ALTER COLUMN sky_status SET NOT NULL,
    ALTER COLUMN forecasted_at SET NOT NULL,
    ALTER COLUMN forecast_at SET NOT NULL,
    ALTER COLUMN location_id SET NOT NULL;
