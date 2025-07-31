package com.stylemycloset.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FollowListResponse<T>(
    @JsonProperty("data")
    List<T> data,
    @JsonProperty("nextCursor")
    String nextCursor,
    @JsonProperty("nextIdAfter")
    String nextIdAfter,
    @JsonProperty("hasNext")
    Boolean hasNext,
    @JsonProperty("totalCount")
    Integer totalCount,
    @JsonProperty("sortBy")
    String sortBy,
    @JsonProperty("sortDirection")
    String sortDirection
) {

}
