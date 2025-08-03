package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.Closet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClosetRepository extends JpaRepository<Closet, Long> {
    Optional<Closet> findByUserId(Long userId);
}
