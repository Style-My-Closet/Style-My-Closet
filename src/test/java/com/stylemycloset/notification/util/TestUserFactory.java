package com.stylemycloset.notification.util;

import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

public final class TestUserFactory {

  public static User createUser(String name, String email, Long id) {
    UserCreateRequest request = new UserCreateRequest(name, email, "test");
    User user = new User(request);
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }
}
