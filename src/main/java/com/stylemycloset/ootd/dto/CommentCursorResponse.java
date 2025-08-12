package com.stylemycloset.ootd.dto;

import java.util.List;

public record CommentCursorResponse(
    List<CommentDto> data,
    String nextCursor,
    Long nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    String sortDirection
) {

}