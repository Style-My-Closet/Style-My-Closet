package com.stylemycloset.common.repository.cursor;

public record NextCursorInfo(
    String nextCursor,
    String nextIdAfter
) {

}