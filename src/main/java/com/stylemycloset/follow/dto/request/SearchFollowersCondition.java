package com.stylemycloset.follow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Sort.Direction;

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
    Direction sortDirection
) {

}
