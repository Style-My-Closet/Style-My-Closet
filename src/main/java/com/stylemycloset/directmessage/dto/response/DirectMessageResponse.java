package com.stylemycloset.directmessage.dto.response;

import java.util.List;

public record DirectMessageResponse<T>(
    List<T> data,
    String nextCursor,
    String nextIdAfter,
    Boolean hasNext,
    Integer totalCount,
    String sortBy,
    String sortDirection
) {

}
