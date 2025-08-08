package com.stylemycloset.follow.dto.request;

public record SearchFollowingsCondition(
    Long followerId,
    String cursor,
    String idAfter,
    Integer limit,
    String nameLike,
    String sortBy,
    String sortDirection
) {

}
