package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.Cloth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothRepository extends JpaRepository<Cloth, Long>, ClothRepositoryCustom {

} 