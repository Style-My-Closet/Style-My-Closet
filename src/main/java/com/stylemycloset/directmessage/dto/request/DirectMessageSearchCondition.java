package com.stylemycloset.directmessage.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.Sort.Direction;

public record DirectMessageSearchCondition(
    @Positive
    @NotNull
    Long userId,
    String cursor,
    String idAfter,
    Integer limit,
    String sortBy,
    Direction sortDirection
) {

}
