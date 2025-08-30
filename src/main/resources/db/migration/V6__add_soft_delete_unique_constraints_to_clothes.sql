-- clothes: owner_id + definitionName, 활성만 유니크
CREATE UNIQUE INDEX IF NOT EXISTS uq_clothes_owner_clothes_name_active
    ON clothes(owner_id, name)
    WHERE deleted_at IS NULL;

-- clothes_attribute_definition: definitionName, 활성만 유니크
CREATE UNIQUE INDEX IF NOT EXISTS uq_attribute_definition_name_active
    ON clothes_attribute_definition(name)
    WHERE deleted_at IS NULL;

-- clothes_attribute_selectable_value: definitionName, 활성만 유니크
CREATE UNIQUE INDEX IF NOT EXISTS uq_attribute_selectable_value_active
    ON clothes_attribute_selectable_value(attribute_definition_id, value)
    WHERE deleted_at IS NULL;

-- clothes_attribute_selected_value: clothes_id + attribute_selectable_id, 활성만 유니크
CREATE UNIQUE INDEX IF NOT EXISTS uq_clothing_attribute_active
    ON clothes_attribute_selected_value(clothes_id, attribute_selectable_id)
    WHERE deleted_at IS NULL;