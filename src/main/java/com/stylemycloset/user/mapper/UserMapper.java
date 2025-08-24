package com.stylemycloset.user.mapper;

import com.stylemycloset.user.dto.data.ProfileDto;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserDto toUserDto(User user) {
    if (user == null) {
      return null;
    }

    return new UserDto(user.getId(),
        user.getCreatedAt(),
        user.getEmail(),
        user.getName(),
        user.getRole(),
        user.getLinkedOAuthProviders(),
        user.isLocked());
  }

  public ProfileDto toProfileDto(User user, String profileImageUrl) {
    if (user == null) {
      return null;
    }

    return new ProfileDto(user.getId(),
        user.getName(),
        user.getGender(),
        user.getBirthDate(),
        user.getLocation(),
        user.getTemperatureSensitivity(),
        profileImageUrl
    );
  }
}
