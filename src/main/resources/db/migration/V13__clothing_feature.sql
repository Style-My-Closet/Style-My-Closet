CREATE TABLE clothing_conditions (
                                     id BIGSERIAL PRIMARY KEY,
                                     temperature DOUBLE PRECISION NOT NULL,
                                     wind_speed DOUBLE PRECISION NOT NULL,
                                     humidity DOUBLE PRECISION NOT NULL,
                                     gender SMALLINT NOT NULL,
                                     temperature_sensitivity INTEGER,
                                     sky_status SMALLINT NOT NULL,
                                     weather_type SMALLINT ,
                                     color SMALLINT,
                                     sleeve_length SMALLINT,
                                     pants_length SMALLINT,
                                     label BOOLEAN
);