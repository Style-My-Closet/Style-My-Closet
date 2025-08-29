DROP INDEX IF EXISTS uq_clothes_owner_clothes_name_active;

-- clothes: owner_id + clothesName, clothes_type 활성만 유니크
CREATE UNIQUE INDEX IF NOT EXISTS uq_clothes_owner_clothes_name_clothes_type_active
    ON clothes(owner_id, name, clothes_type)
    WHERE deleted_at IS NULL;