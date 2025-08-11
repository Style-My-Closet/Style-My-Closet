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

-- Batch Job Instance
CREATE TABLE batch_job_instance (
                                    job_instance_id BIGINT NOT NULL PRIMARY KEY,
                                    version BIGINT,
                                    job_name VARCHAR(100) NOT NULL,
                                    job_key VARCHAR(32) NOT NULL,
                                    CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
);

-- Batch Job Execution
CREATE TABLE batch_job_execution (
                                     job_execution_id BIGINT NOT NULL PRIMARY KEY,
                                     version BIGINT,
                                     job_instance_id BIGINT NOT NULL,
                                     create_time TIMESTAMP NOT NULL,
                                     start_time TIMESTAMP DEFAULT NULL,
                                     end_time TIMESTAMP DEFAULT NULL,
                                     status VARCHAR(10),
                                     exit_code VARCHAR(2500),
                                     exit_message VARCHAR(2500),
                                     last_updated TIMESTAMP,
                                     job_configuration_location VARCHAR(2500) NULL,
                                     CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id)
                                         REFERENCES batch_job_instance(job_instance_id)
);

-- Batch Job Execution Params
CREATE TABLE batch_job_execution_params (
                                            job_execution_id BIGINT NOT NULL,
                                            type_cd VARCHAR(6) NOT NULL,
                                            key_name VARCHAR(100) NOT NULL,
                                            string_val VARCHAR(250),
                                            date_val TIMESTAMP DEFAULT NULL,
                                            long_val BIGINT,
                                            double_val DOUBLE PRECISION,
                                            identifying CHAR(1) NOT NULL,
                                            CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id)
                                                REFERENCES batch_job_execution(job_execution_id)
);

-- Batch Step Execution
CREATE TABLE batch_step_execution (
                                      step_execution_id BIGINT NOT NULL PRIMARY KEY,
                                      version BIGINT NOT NULL,
                                      step_name VARCHAR(100) NOT NULL,
                                      job_execution_id BIGINT NOT NULL,
                                      start_time TIMESTAMP NOT NULL,
                                      end_time TIMESTAMP DEFAULT NULL,
                                      status VARCHAR(10),
                                      commit_count BIGINT,
                                      read_count BIGINT,
                                      filter_count BIGINT,
                                      write_count BIGINT,
                                      read_skip_count BIGINT,
                                      write_skip_count BIGINT,
                                      process_skip_count BIGINT,
                                      rollback_count BIGINT,
                                      exit_code VARCHAR(2500),
                                      exit_message VARCHAR(2500),
                                      last_updated TIMESTAMP,
                                      CONSTRAINT step_exec_fk FOREIGN KEY (job_execution_id)
                                          REFERENCES batch_job_execution(job_execution_id)
);

-- Batch Step Execution Context
CREATE TABLE batch_step_execution_context (
                                              step_execution_id BIGINT NOT NULL PRIMARY KEY,
                                              short_context VARCHAR(2500) NOT NULL,
                                              serialized_context TEXT,
                                              CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id)
                                                  REFERENCES batch_step_execution(step_execution_id)
);

-- Batch Job Execution Context
CREATE TABLE batch_job_execution_context (
                                             job_execution_id BIGINT NOT NULL PRIMARY KEY,
                                             short_context VARCHAR(2500) NOT NULL,
                                             serialized_context TEXT,
                                             CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id)
                                                 REFERENCES batch_job_execution(job_execution_id)
);

-- Sequences
CREATE SEQUENCE batch_job_seq START 1 MINVALUE 1;
CREATE SEQUENCE batch_job_execution_seq START 1 MINVALUE 1;
CREATE SEQUENCE batch_step_execution_seq START 1 MINVALUE 1;


