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
    updated_at  TIMESTAMP WITH TIME ZONE
);

-- message
CREATE TABLE messages
(
    id          BIGSERIAL PRIMARY KEY,
    sender_id   BIGINT                   NOT NULL,
    receiver_id BIGINT                   NOT NULL,
    content     TEXT                     NOT NULL,
    sent_at     TIMESTAMP WITH TIME ZONE NOT NULL,
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
    role                    VARCHAR(10)              NOT NULL,
    locked                  BOOLEAN                  NOT NULL,
    gender                  VARCHAR(10),
    birth_date              DATE,
    temperature_sensitivity INTEGER,
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

-- closet
CREATE TABLE closets
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                   NOT NULL UNIQUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- category
CREATE TABLE clothes_categories
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50)              NOT NULL UNIQUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- clothes
CREATE TABLE clothes
(
    id          BIGSERIAL PRIMARY KEY,
    closet_id   BIGINT                   NOT NULL,
    category_id BIGINT                   NOT NULL,
    image_id    UUID,
    name        VARCHAR(100)             NOT NULL,
    deleted_at  TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE
);

-- cloth_attribute : 계절 등 큰 카테고리
CREATE TABLE clothes_attributes_categories
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100)             NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- cloth_attribute_option : 계절의 여름, 가을, 겨울
CREATE TABLE clothes_attributes_category_options
(
    id           BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT                   NOT NULL,
    value        VARCHAR(50)              NOT NULL UNIQUE,
    deleted_at   TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE
);

-- cloth_attribute_mapping : 여름, 가을 세부적인 속성 옵션들이 옷과 매핑이 되어있는걸 표현
CREATE TABLE clothes_to_attribute_options
(
    id           BIGSERIAL PRIMARY KEY,
    cloth_id     BIGINT                   NOT NULL,
    attribute_id BIGINT                   NOT NULL,
    option_id    BIGINT                   NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uq_clothing_attribute UNIQUE (cloth_id, attribute_id, option_id)
);


---------------- binary_contents ---------------

-- binary_content
CREATE TABLE binary_contents
(
    id           UUID PRIMARY KEY,
    file_name    varchar(200)             NOT NULL,
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

    sky_status                         VARCHAR(20)              NOT NULL,

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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE BATCH_JOB_INSTANCE  (
                                     JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
                                     VERSION BIGINT ,
                                     JOB_NAME VARCHAR(100) NOT NULL,
                                     JOB_KEY VARCHAR(32) NOT NULL,
                                     CONSTRAINT JOB_INST_UN UNIQUE (JOB_NAME, JOB_KEY)
);

CREATE TABLE BATCH_JOB_EXECUTION  (
                                      JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                      VERSION BIGINT  NOT NULL,
                                      JOB_INSTANCE_ID BIGINT NOT NULL,
                                      CREATE_TIME TIMESTAMP NOT NULL,
                                      START_TIME TIMESTAMP DEFAULT NULL,
                                      END_TIME TIMESTAMP DEFAULT NULL,
                                      STATUS VARCHAR(10),
                                      EXIT_CODE VARCHAR(2500),
                                      EXIT_MESSAGE VARCHAR(2500),
                                      LAST_UPDATED TIMESTAMP,
                                      JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
                                      CONSTRAINT JOB_INST_EXEC_FK FOREIGN KEY (JOB_INSTANCE_ID)
                                          REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
                                             JOB_EXECUTION_ID BIGINT NOT NULL ,
                                             PARAMETER_NAME VARCHAR(100) NOT NULL ,
                                             PARAMETER_TYPE VARCHAR(100) NOT NULL ,
                                             PARAMETER_VALUE VARCHAR(2500) ,
                                             IDENTIFYING CHAR(1) NOT NULL ,
                                             CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID)
                                                 REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION  (
                                       STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                       VERSION BIGINT NOT NULL,
                                       STEP_NAME VARCHAR(100) NOT NULL,
                                       JOB_EXECUTION_ID BIGINT NOT NULL,
                                       START_TIME TIMESTAMP NOT NULL ,
                                       END_TIME TIMESTAMP DEFAULT NULL,
                                       STATUS VARCHAR(10),
                                       COMMIT_COUNT BIGINT ,
                                       READ_COUNT BIGINT ,
                                       FILTER_COUNT BIGINT ,
                                       WRITE_COUNT BIGINT ,
                                       READ_SKIP_COUNT BIGINT ,
                                       WRITE_SKIP_COUNT BIGINT ,
                                       PROCESS_SKIP_COUNT BIGINT ,
                                       ROLLBACK_COUNT BIGINT ,
                                       EXIT_CODE VARCHAR(2500) ,
                                       EXIT_MESSAGE VARCHAR(2500) ,
                                       LAST_UPDATED TIMESTAMP,
                                       CONSTRAINT STEP_EXEC_FK FOREIGN KEY (JOB_EXECUTION_ID)
                                           REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
                                               STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                               SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                               SERIALIZED_CONTEXT TEXT ,
                                               CONSTRAINT STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID)
                                                   REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
                                              JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                              SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                              SERIALIZED_CONTEXT TEXT ,
                                              CONSTRAINT JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID)
                                                  REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE SEQUENCE BATCH_JOB_SEQ START 1 MINVALUE 1;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ START 1 MINVALUE 1;
CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ START 1 MINVALUE 1;
