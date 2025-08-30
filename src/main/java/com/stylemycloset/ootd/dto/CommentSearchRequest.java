package com.stylemycloset.ootd.dto;

import jakarta.validation.constraints.NotNull;

public record CommentSearchRequest(
    String cursor,
    Long idAfter,
    @NotNull(message = "limit 값은 필수입니다.")
    Integer limit
) {

}
