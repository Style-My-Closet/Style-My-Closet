------------- follows unique constraint ------------
ALTER TABLE follows
    ADD CONSTRAINT uq_follower_followee UNIQUE (follower_id, followee_id);


