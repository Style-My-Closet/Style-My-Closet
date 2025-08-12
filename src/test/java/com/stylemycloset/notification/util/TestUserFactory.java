package com.stylemycloset.notification.util;

import com.stylemycloset.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

public final class TestUserFactory {

  public static User createUser(String name, String email, Long id) {
    User user = new User(name, email, "test");
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }
}
