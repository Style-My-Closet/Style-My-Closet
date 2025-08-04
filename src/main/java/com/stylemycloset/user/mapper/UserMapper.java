package com.stylemycloset.user.mapper;


import com.stylemycloset.user.dto.data.ProfileDto;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDto UsertoUserDto(User user);

  ProfileDto UsertoProfileDto(User user);
}
