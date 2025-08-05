-- Drop the existing table if it exists
DROP TABLE IF EXISTS clothes_attributes_category_options;

-- Recreate the table with the correct structure
CREATE TABLE clothes_attributes_category_options
(
    id           BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT                   NOT NULL,
    value        VARCHAR(50)              NOT NULL,
    deleted_at   TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT uq_attribute_option UNIQUE (attribute_id, value)
); 