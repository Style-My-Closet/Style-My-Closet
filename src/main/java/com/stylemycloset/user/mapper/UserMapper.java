package com.stylemycloset.user.mapper;

import com.stylemycloset.location.mapper.LocationMapper;
import com.stylemycloset.user.dto.data.ProfileDto;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.dto.WeatherAPILocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final LocationMapper locationMapper;

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
    WeatherAPILocation locationDto = null;
    if (user.getLocation() != null) {
      locationDto = locationMapper.toDto(user.getLocation());
    }

    return new ProfileDto(user.getId(),
        user.getName(),
        user.getGender(),
        user.getBirthDate(),
        locationDto,
        user.getTemperatureSensitivity(),
        profileImageUrl
    );
  }
}
