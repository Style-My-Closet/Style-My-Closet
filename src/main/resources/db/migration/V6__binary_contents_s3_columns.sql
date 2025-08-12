-- Add S3 related columns and soft delete to binary_contents
ALTER TABLE IF EXISTS binary_contents
  ADD COLUMN IF NOT EXISTS image_url VARCHAR(500),
  ADD COLUMN IF NOT EXISTS object_key VARCHAR(300),
  ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;


