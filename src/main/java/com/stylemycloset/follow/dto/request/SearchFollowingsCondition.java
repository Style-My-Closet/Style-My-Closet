package com.stylemycloset.follow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SearchFollowingsCondition(
    @Positive
    @NotNull
    Long followerId,
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
