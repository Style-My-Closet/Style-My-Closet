-- Remove object_key column from binary_contents table
-- UUID ID is now used directly as S3 object key

ALTER TABLE binary_contents DROP COLUMN IF EXISTS object_key;
