package com.stylemycloset.cloth.dto;

public record CursorDto(
        Long cursor,
        Long idAfter,
        Integer limit,
        String sortBy,
        String sortDirection,
        String keywordLike
) {
    private static final int DEFAULT_LIMIT = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIRECTION = "ASCENDING";

    public static CursorDto ofDefault(int limit) {
        return new CursorDto(null, null, limit, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION, null);
    }

    public static CursorDto ofSearch(String keywordLike, int limit) {
        return new CursorDto(null, null, limit, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION, keywordLike);
    }
    
    public static CursorDto ofPagination(Long cursor, int limit) {
        return new CursorDto(cursor, null, limit, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION, null);
    }

    
    // 기본값 처리
    public Integer limit() {
        return limit != null ? limit : DEFAULT_LIMIT;
    }
    
    public String sortBy() {
        return sortBy != null ? sortBy : DEFAULT_SORT_BY;
    }
    
    public String sortDirection() {
        return sortDirection != null ? sortDirection : DEFAULT_SORT_DIRECTION;
    }
    
    public boolean isDescending() {
        return !"ASCENDING".equalsIgnoreCase(sortDirection());
    }
}
