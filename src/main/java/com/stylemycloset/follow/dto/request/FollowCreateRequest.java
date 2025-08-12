package com.stylemycloset.follow.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FollowCreateRequest(
    @Positive
    @NotNull
    Long followeeId,
    @Positive
    @NotNull
    Long followerId
) {

}
