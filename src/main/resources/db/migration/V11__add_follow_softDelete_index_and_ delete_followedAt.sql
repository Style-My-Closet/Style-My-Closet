ALTER TABLE follows DROP COLUMN followed_at;

ALTER TABLE follows DROP CONSTRAINT uq_follower_followee;

CREATE UNIQUE INDEX IF NOT EXISTS uq_follower_followee
    ON follows(follower_id, followee_id)
    WHERE deleted_at IS NULL;