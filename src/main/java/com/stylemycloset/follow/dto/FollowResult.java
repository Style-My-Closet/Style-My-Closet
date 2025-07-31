package com.stylemycloset.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FollowResult(
    @JsonProperty("id")
    Long id,
    @JsonProperty("followee")
    FollowUserInfo followee,
    @JsonProperty("follower")
    FollowUserInfo follower
) {

}
