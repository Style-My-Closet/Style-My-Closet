package com.stylemycloset.directmessage.dto.request;

import jakarta.validation.constraints.NotNull;

public record DirectMessageSearchCondition(
    @NotNull
    Long userId,
    String cursor,
    String idAfter,
    Integer limit,
    String sortBy,
    String sortDirection
) {

}
