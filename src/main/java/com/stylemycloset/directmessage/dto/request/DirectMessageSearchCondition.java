package com.stylemycloset.directmessage.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DirectMessageSearchCondition(
    @Positive
    @NotNull
    Long userId,
    String cursor,
    String idAfter,
    Integer limit,
    String sortBy,
    String sortDirection
) {

}
