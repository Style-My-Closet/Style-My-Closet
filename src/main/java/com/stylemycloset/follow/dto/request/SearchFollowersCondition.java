package com.stylemycloset.follow.dto.request;

public record SearchFollowersCondition(
    Long followeeId,
    String cursor,
    String idAfter,
    Integer limit,
    String nameLike
) {

}
