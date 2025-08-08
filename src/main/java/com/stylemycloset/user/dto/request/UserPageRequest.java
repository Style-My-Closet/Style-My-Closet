package com.stylemycloset.user.dto.request;

import com.stylemycloset.user.entity.Role;

public record UserPageRequest(
    String cursor,
    Long idAfter,
    Integer limit,
    String sortBy,
    String sortDirection,
    String emailLike,
    Role roleEqual,
    Boolean locked
) {

}
