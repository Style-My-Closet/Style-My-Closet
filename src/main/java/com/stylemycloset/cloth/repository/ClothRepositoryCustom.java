package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.Cloth;

import java.util.List;
import java.util.Optional;

public interface ClothRepositoryCustom {
    Optional<Cloth> findByIdWithAttributes(Long clothId);
    List<Cloth> findWithCursorPagination(Long userId, Long idAfter, int limitPlusOne, 
                                        boolean isDescending, boolean hasIdAfter);
    long countByUserId(Long userId);
} 