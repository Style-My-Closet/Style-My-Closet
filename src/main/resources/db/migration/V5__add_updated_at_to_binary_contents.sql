-- Ensure binary_contents has updated_at column required by JPA auditing and SQLDelete
ALTER TABLE IF EXISTS binary_contents
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();


