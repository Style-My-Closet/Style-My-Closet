package com.stylemycloset.cloth.dto.response;

import com.stylemycloset.cloth.dto.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> data;
    private PaginationInfo pagination;
  
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private String nextCursor;
        private String nextIdAfter;
        private boolean hasNext;
        private long totalCount;
        private String sortBy;
        private SortDirection sortDirection;
    }
    
    
    public static <T> PaginatedResponse<T> of(
            List<T> data,
            String nextCursor,
            String nextIdAfter,
            boolean hasNext,
            long totalCount,
            String sortBy,
            SortDirection sortDirection
    ) {
        PaginationInfo paginationInfo = PaginationInfo.builder()
                .nextCursor(nextCursor)
                .nextIdAfter(nextIdAfter)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        
        return PaginatedResponse.<T>builder()
                .data(data)
                .pagination(paginationInfo)
                .build();
    }
    
   
    public static <T> PaginatedResponse<T> of(
            List<T> data,
            boolean hasNext,
            long totalCount
    ) {
        return of(data, null, null, hasNext, totalCount, "id", SortDirection.ASCENDING);
    }
} 