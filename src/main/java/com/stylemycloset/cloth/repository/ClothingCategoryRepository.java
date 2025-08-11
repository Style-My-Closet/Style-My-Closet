package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.ClothingCategory;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothingCategoryRepository extends JpaRepository<ClothingCategory, Long> {
    Optional<ClothingCategory> findByName(ClothingCategoryType name);
}
