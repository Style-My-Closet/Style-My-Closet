------------------ follow & DM ------------------
-- follow
CREATE TABLE follow
(
    id           BIGSERIAL PRIMARY KEY,
    follower_id  BIGINT                   NOT NULL,
    following_id BIGINT                   NOT NULL,
    followed_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at   TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE,
);

-- message
CREATE TABLE message
(
    id          BIGSERIAL PRIMARY KEY,
    sender_id   BIGINT                   NOT NULL,
    receiver_id BIGINT                   NOT NULL,
    content     TEXT                     NOT NULL,
    sent_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at  TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,
);

------------------ notification ------------------

-- notification
CREATE TABLE notifications
(
    id          BIGSERIAL PRIMARY KEY,
    receiver_id BIGINT                   NOT NULL,
    title       VARCHAR(100)             NOT NULL,
    content     TEXT,
    level       VARCHAR(10)              NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

-------------------- user --------------------
-- user
CREATE TABLE users
(
    user_id                 BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(20)              NOT NULL,
    email                   VARCHAR(30)              NOT NULL UNIQUE,
    role                    VARCHAR(10)              NOT NULL,
    locked                  BOOLEAN                  NOT NULL,
    gender                  VARCHAR(10),
    birth_date              DATE,
    temperature_sensitivity INTEGER,
    create_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    update_at               TIMESTAMP WITH TIME ZONE,
    delete_at               TIMESTAMP WITH TIME ZONE
);

-- location
CREATE TABLE locations
(
    id             BIGSERIAL PRIMARY KEY,
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    x              INTEGER,
    y              INTEGER,
    location_names JSONB
);

------------------- cloth --------------------
-- closet

-- category

-- cloth

-- cloth_attribute

-- cloth_attribute_option

-- cloth_attribute_mapping

-- binary_content
CREATE TABLE binary_content
(
    id           UUID PRIMARY KEY,
    file_name    varchar(200)             NOT NULL,
    content_type VARCHAR(100),
    size         BIGINT                   NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

------------------ weather -------------------

-- weather

CREATE SEQUENCE weather_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE weather (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    forecasted_at TIMESTAMP NOT NULL,
    forecast_at TIMESTAMP NOT NULL,

    -- ENUM  # VARCHAR : 이점 한눈에 구조가 보여서 좋다.
    sky_status VARCHAR(20) NOT NULL CHECK (sky_status IN ('CLEAR', 'MOSTLY_CLOUDY', 'CLOUDY')),

    -- Precipitation
    precipitation_type VARCHAR(20),
    precipitation_amount DOUBLE PRECISION,
    precipitation_probability DOUBLE PRECISION,

    -- Temperature
    temperature_current DOUBLE PRECISION,
    temperature_compared_to_day_before DOUBLE PRECISION,
    temperature_min DOUBLE PRECISION,
    temperature_max DOUBLE PRECISION,

    -- Humidity
    humidity_current DOUBLE PRECISION,
    humidity_compared_to_day_before DOUBLE PRECISION,

    -- WindSpeed
    wind_speed_current DOUBLE PRECISION,
    wind_speed_compared_to_day_before DOUBLE PRECISION,

    -- 알림 관련
    is_alert_triggered BOOLEAN DEFAULT FALSE,
    alert_type VARCHAR(20) CHECK (alert_type IN ('NONE', 'RAIN', 'HEAVY_RAIN', 'HIGH_TEMP', 'LOW_TEMP', 'STRONG_WIND')),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);

CREATE TABLE location (
    id BIGSERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    x INTEGER,
    y INTEGER
);

------------------- feed ---------------------

-- feeds

-- feed_ootd_clothes

-- feed_comments

-- feed_likes

-- comment_likes




