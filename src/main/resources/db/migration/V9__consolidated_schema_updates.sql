


ALTER TABLE IF EXISTS clothes_to_attribute_options
  ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE IF EXISTS binary_contents
  ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

ALTER TABLE IF EXISTS binary_contents
  ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE IF EXISTS clothes_attributes_category_options
  DROP CONSTRAINT IF EXISTS clothes_attributes_category_options_value_key;

ALTER TABLE IF EXISTS clothes_attributes_category_options
  DROP CONSTRAINT IF EXISTS uq_attribute_option;

CREATE UNIQUE INDEX IF NOT EXISTS uq_attr_value_active
  ON clothes_attributes_category_options(attribute_id, value)
  WHERE deleted_at IS NULL;


