package com.stylemycloset.user.repository;

import com.stylemycloset.user.dto.request.UserPageRequest;
import com.stylemycloset.user.entity.User;
import java.util.List;

public interface UserRepositoryCustom {

  List<User> findUsersByCursor(UserPageRequest request);

  Integer countByFilter(UserPageRequest request);

}
