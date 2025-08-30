package com.stylemycloset.ootd.dto;

import java.time.Instant;

public record CommentDto(
    Long id,
    Instant createdAt,
    Long feedId,
    AuthorDto author,
    String content
) {

}
