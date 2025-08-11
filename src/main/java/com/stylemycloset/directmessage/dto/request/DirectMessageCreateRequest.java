package com.stylemycloset.directmessage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DirectMessageCreateRequest(
    @NotNull
    Long senderId,
    @NotNull
    Long receiverId,
    @NotBlank
    String content
) {

}
