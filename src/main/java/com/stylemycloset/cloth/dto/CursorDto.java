package com.stylemycloset.cloth.dto;

public record CursorDto(
        Long cursor,
        Long idAfter,
        Integer limit,
        String sortBy,
        String sortDirection,
        String keywordLike
) {
    
    public static CursorDto ofDefault() {
        return new CursorDto(null, null, 20, "id", "ASCENDING", null);
    }
    
    public static CursorDto ofDefault(int limit) {
        return new CursorDto(null, null, limit, "id", "ASCENDING", null);
    }
    
    public static CursorDto ofSearch(String keywordLike) {
        return new CursorDto(null, null, 20, "id", "ASCENDING", keywordLike);
    }
    
    public static CursorDto ofSearch(String keywordLike, int limit) {
        return new CursorDto(null, null, limit, "id", "ASCENDING", keywordLike);
    }
    
    public static CursorDto ofPagination(Long cursor, int limit) {
        return new CursorDto(cursor, null, limit, "id", "ASCENDING", null);
    }
    
    public static CursorDto ofPagination(Long cursor, int limit, String sortBy, String sortDirection) {
        return new CursorDto(cursor, null, limit, sortBy, sortDirection, null);
    }
    
    // String 기반 생성자 (기존 코드와의 호환성을 위해)
    public static CursorDto fromStrings(String cursor, String idAfter, String limit, 
                                       String sortBy, String sortDirection, String keywordLike) {
        return new CursorDto(
                cursor != null ? Long.valueOf(cursor) : null,
                idAfter != null ? Long.valueOf(idAfter) : null,
                limit != null ? Integer.valueOf(limit) : 20,
                sortBy != null ? sortBy : "id",
                sortDirection != null ? sortDirection : "ASCENDING",
                keywordLike
        );
    }
    
    // 기본값 처리
    public Integer limit() {
        return limit != null ? limit : 20;
    }
    
    public String sortBy() {
        return sortBy != null ? sortBy : "deleted_at";
    }
    
    public String sortDirection() {
        return sortDirection != null ? sortDirection : "ASCENDING";
    }
    
    public boolean isDescending() {
        return !"ASCENDING".equalsIgnoreCase(sortDirection());
    }
}
