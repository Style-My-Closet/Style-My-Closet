package com.stylemycloset.user.dto.response;

import com.stylemycloset.user.dto.data.UserDto;
import java.util.List;

public record UserCursorResponse(
    List<UserDto> data,
    String nextCursor,
    Long nextIdAfter,
    boolean hasNext,
    Integer totalCount,
    String sortBy,
    String sortDirection
) {

}
