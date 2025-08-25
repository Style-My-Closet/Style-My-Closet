------------------ follow & DM ------------------

-- follow
CREATE TABLE follows
(
    id          BIGSERIAL PRIMARY KEY,
    follower_id BIGINT                   NOT NULL,
    followee_id BIGINT                   NOT NULL,
    followed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at  TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uq_follower_followee UNIQUE (follower_id, followee_id)
);

-- message
CREATE TABLE messages
(
    id          BIGSERIAL PRIMARY KEY,
    sender_id   BIGINT                   NOT NULL,
    receiver_id BIGINT                   NOT NULL,
    content     TEXT                     NOT NULL,
    deleted_at  TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE
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
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(20)              NOT NULL,
    email                   VARCHAR(30)              NOT NULL UNIQUE,
    password                VARCHAR(60)              NOT NULL,
    role                    VARCHAR(10)              NOT NULL,
    locked                  BOOLEAN                  NOT NULL,
    gender                  VARCHAR(10),
    birth_date              DATE,
    temperature_sensitivity INTEGER,
    profile_id              UUID,
    deleted_at              TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE,

    location_id             BIGINT
);

-- location
CREATE TABLE locations
(
    id             BIGSERIAL PRIMARY KEY,
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    x              INTEGER,
    y              INTEGER,
    location_names JSONB,

    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE
);

------------------- cloth --------------------

-- clothes
CREATE TABLE clothes
(
    id           BIGSERIAL PRIMARY KEY,
    owner_id     BIGINT                   NOT NULL,
    name         VARCHAR(100)             NOT NULL,
    image_id     UUID,
    clothes_type varchar(50)              NOT NULL,
    deleted_at   TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE
);

-- cloth_attribute : 계절등 큰 카테고리의 이름
CREATE TABLE clothes_attribute_definition
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100)             NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- cloth_attribute_selectable_value : 큰 속성 내에 선택할 수 있는 것 - 여름, 가을, 겨울
CREATE TABLE clothes_attribute_selectable_value
(
    id                      BIGSERIAL PRIMARY KEY,
    attribute_definition_id BIGINT                   NOT NULL,
    value                   VARCHAR(50)              NOT NULL,
    deleted_at              TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE
);

-- cloth_attribute_selected_value: 옷과 매핑된  한 카테고리 내부네서 선택할 수 있는 값 -> 롤렉스 - 빨강
CREATE TABLE clothes_attribute_selected_value
(
    id                      BIGSERIAL PRIMARY KEY,
    clothes_id              BIGINT                   NOT NULL,
    attribute_selectable_id BIGINT                   NOT NULL,
    deleted_at              TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE
);

---------------- binary_contents ---------------

-- binary_content
CREATE TABLE binary_contents
(
    id           UUID PRIMARY KEY,
    original_name    varchar(200)             NOT NULL,
    content_type VARCHAR(100),
    size         BIGINT                   NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

------------------ weather -------------------

-- weather
CREATE TABLE weather
(
    id                                 BIGSERIAL PRIMARY KEY,
    forecasted_at                      TIMESTAMP WITH TIME ZONE NOT NULL,
    forecast_at                        TIMESTAMP WITH TIME ZONE NOT NULL,

    sky_status                         VARCHAR(20),

    precipitation_type                 VARCHAR(20),
    precipitation_amount               DOUBLE PRECISION,
    precipitation_probability          DOUBLE PRECISION,

    temperature_current                DOUBLE PRECISION,
    temperature_compared_to_day_before DOUBLE PRECISION,
    temperature_min                    DOUBLE PRECISION,
    temperature_max                    DOUBLE PRECISION,

    humidity_current                   DOUBLE PRECISION,
    humidity_compared_to_day_before    DOUBLE PRECISION,

    wind_speed_current                 DOUBLE PRECISION,
    wind_speed_compared_to_day_before  DOUBLE PRECISION,

    is_alert_triggered                 BOOLEAN DEFAULT FALSE,
    alert_type                         VARCHAR(20) CHECK (alert_type IN
                                                          ('NONE', 'RAIN', 'HEAVY_RAIN',
                                                           'HIGH_TEMP', 'LOW_TEMP',
                                                           'STRONG_WIND')),

    created_at                         TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                         TIMESTAMP WITH TIME ZONE,
    location_id                        BIGINT                   NOT NULL,

    FOREIGN KEY (location_id) REFERENCES locations (id)
);

------------------- feed ---------------------

-- feeds
CREATE TABLE feeds
(
    id         BIGSERIAL PRIMARY KEY,
    author_id  BIGINT                   NOT NULL,
    weather_id BIGINT,
    content    TEXT                     NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- feed_ootd_clothes
CREATE TABLE feed_ootd_clothes
(
    id         BIGSERIAL PRIMARY KEY,
    feed_id    BIGINT                   NOT NULL,
    clothes_id BIGINT                   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_feed_clothes UNIQUE (feed_id, clothes_id)
);

-- feed_comments
CREATE TABLE feed_comments
(
    id         BIGSERIAL PRIMARY KEY,
    feed_id    BIGINT                   NOT NULL,
    author_id  BIGINT                   NOT NULL,
    content    TEXT                     NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- feed_likes
CREATE TABLE feed_likes
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                   NOT NULL,
    feed_id    BIGINT                   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- comment_likes
CREATE TABLE comment_likes
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                   NOT NULL,
    comment_id BIGINT                   NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);




