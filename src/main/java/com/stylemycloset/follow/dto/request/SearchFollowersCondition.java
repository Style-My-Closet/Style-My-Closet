package com.stylemycloset.follow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SearchFollowersCondition(
    @Positive
    @NotNull
    Long followeeId,
    String cursor,
    String idAfter,
    @Positive
    @NotNull
    Integer limit,
    String nameLike,
    String sortBy,
    String sortDirection
) {

}
