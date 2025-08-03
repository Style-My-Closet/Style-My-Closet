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
public class AttributeListResponseDto {
    private List<AttributeResponseDto> data;
    private String nextCursor;
    private String nextIdAfter;
    private boolean hasNext;
    private long totalCount;
    private String sortBy;
    private SortDirection sortDirection;
}