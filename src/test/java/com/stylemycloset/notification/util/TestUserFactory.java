package com.stylemycloset.notification.util;

import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;

public final class TestUserFactory {

  public static User createUser(UserRepository userRepository, String name, String email) {
    User user = new User(name, email, "test");
    userRepository.save(user);
    return user;
  }
}
