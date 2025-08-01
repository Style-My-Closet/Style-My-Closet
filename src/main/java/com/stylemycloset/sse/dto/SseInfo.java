package com.stylemycloset.sse.dto;

public record SseInfo(
    long id,
    String name,
    Object data,
    long createdAt
) {

}
