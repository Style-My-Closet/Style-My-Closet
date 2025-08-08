package com.stylemycloset.user.mapper;


import com.stylemycloset.user.dto.data.ProfileDto;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDto UsertoUserDto(User user);

  @Mapping(source = "id", target = "userId")
  ProfileDto UsertoProfileDto(User user);
}
