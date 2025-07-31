package com.stylemycloset.ootd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
    @NotNull(message = "피드 ID는 필수입니다.")
    Long feedId,
    @NotNull(message = "작성자 ID는 필수입니다.")
    Long authorId,
    @NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
    String content
) {

}


