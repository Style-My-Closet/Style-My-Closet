package com.stylemycloset.follow.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FollowCreateRequest(
    @JsonProperty("followeeId")
    Long followeeId,
    @JsonProperty("followerId")
    Long followerId
) {

}
