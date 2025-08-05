package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.ClothingAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClothingAttributeRepository extends JpaRepository<ClothingAttribute, Long>, ClothingAttributeRepositoryCustom {
    
    Optional<ClothingAttribute> findByName(String name);
    
    boolean existsByName(String name);
} 