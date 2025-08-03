package com.stylemycloset.cloth.dto;

public record CursorDto
        (
             String cursor,
             String idAfter,
             String limit,
             String sortBy,
             String sortDirection,
             String keywordLike
        ){
}
