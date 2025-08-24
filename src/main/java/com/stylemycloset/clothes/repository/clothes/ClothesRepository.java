package com.stylemycloset.clothes.repository.clothes;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.repository.clothes.impl.ClothesRepositoryCustom;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, Long>, ClothesRepositoryCustom {

}