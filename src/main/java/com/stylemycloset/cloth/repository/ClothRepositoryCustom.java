package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.response.ClothItemDto;

import java.util.List;
import java.util.Optional;

public interface ClothRepositoryCustom {

    long countByUserId(Long userId);

    // DTO 프로젝션 기반 조회 메서드들
    List<ClothItemDto> findClothItemDtosWithCursorPagination(Long userId, Long idAfter, int limitPlusOne,
                                                             boolean isDescending, boolean hasIdAfter);
    Optional<ClothResponseDto> findClothResponseDtoById(Long clothId);
}