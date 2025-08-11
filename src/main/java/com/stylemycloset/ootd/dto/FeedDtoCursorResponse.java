package com.stylemycloset.ootd.dto;

import java.util.List;

public record FeedDtoCursorResponse(
    List<FeedDto> data,
    String nextCursor,
    String nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    String sortDirection
) {

}
