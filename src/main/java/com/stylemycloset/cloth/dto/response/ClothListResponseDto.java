package com.stylemycloset.cloth.dto.response;

import com.stylemycloset.cloth.dto.SortDirection;

import java.util.List;

public record ClothListResponseDto(
        List<ClothItemDto> data,
        String nextCursor,
        String nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        SortDirection sortDirection
) {
    
    public static ClothListResponseDto of(
            List<ClothItemDto> data,
            String nextCursor,
            String nextIdAfter,
            boolean hasNext,
            long totalCount,
            String sortBy,
            SortDirection sortDirection
    ) {
        return new ClothListResponseDto(data, nextCursor, nextIdAfter, hasNext, totalCount, sortBy, sortDirection);
    }
}
