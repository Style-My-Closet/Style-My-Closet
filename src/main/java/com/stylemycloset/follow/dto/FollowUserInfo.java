package com.stylemycloset.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FollowUserInfo(
    @JsonProperty("userId")
    Long userId,
    @JsonProperty("name")
    String name,
    @JsonProperty("profileImageUrl")
    String profileImageUrl
) {

}
