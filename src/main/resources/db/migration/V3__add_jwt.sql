CREATE TABLE jwt_sessions
(
    id              BIGSERIAL PRIMARY KEY,
    created_at      timestamp with time zone NOT NULL,
    updated_at      timestamp with time zone,

    user_id         BIGINT                NOT NULL,
    access_token    TEXT UNIQUE              NOT NULL,
    refresh_token   TEXT UNIQUE              NOT NULL,
    expiration_time timestamp with time zone NOT NULL
);