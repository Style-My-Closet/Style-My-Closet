ALTER TABLE locations ADD CONSTRAINT uq_location_xy UNIQUE(x, y);
ALTER TABLE locations DROP CONSTRAINT uq_location_lat_lon;