package com.stylemycloset.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FollowSummaryResult(
    @JsonProperty("followeeId")
    Long followeeId,
    @JsonProperty("followerCount")
    Integer followerCount,
    @JsonProperty("followingCount")
    Integer followingCount,
    @JsonProperty("followedByMe")
    Boolean followedByMe,
    @JsonProperty("followedByMeId")
    Long followedByMeId,
    @JsonProperty("followingMe")
    Boolean followingMe
) {



}
