package com.stylemycloset.ootd.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FeedCreateRequest(
    @NotNull(message = "작성자 ID는 필수입니다.")
    Long authorId,
    Long weatherId,
    @NotEmpty(message = "의상 ID 목록은 비어있을 수 없습니다.")
    List<Long> clothesIds,
    @NotNull(message = "피드 내용은 필수입니다.")
    String content
) {

}