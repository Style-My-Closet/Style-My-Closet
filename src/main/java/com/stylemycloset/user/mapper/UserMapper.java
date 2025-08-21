package com.stylemycloset.user.mapper;

import com.stylemycloset.user.dto.data.ProfileDto;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  public UserDto UsertoUserDto(User user) {
    return new UserDto(user.getId(),
        user.getCreatedAt(),
        user.getEmail(),
        user.getName(),
        user.getRole(),
        user.getLinkedOAuthProviders(),
        user.isLocked());
  }

  public ProfileDto UsertoProfileDto(User user, String profileImageUrl) {

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
