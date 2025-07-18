package com.example.mapper;

import com.example.model.User;
import com.example.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
    User toEntity(UserDto userDto);
}
