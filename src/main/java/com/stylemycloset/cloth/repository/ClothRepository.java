package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothRepository extends JpaRepository<Cloth, Long>, ClothRepositoryCustom {

    // 단방향 관계로 인해 추가된 메서드들
    List<Cloth> findByCategory(ClothingCategory category);
    
    @Query("SELECT c FROM Cloth c WHERE c.category.id = :categoryId AND c.deletedAt IS NULL")
    List<Cloth> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT COUNT(c) FROM Cloth c WHERE c.category.id = :categoryId AND c.deletedAt IS NULL")
    long countByCategoryId(@Param("categoryId") Long categoryId);

} 