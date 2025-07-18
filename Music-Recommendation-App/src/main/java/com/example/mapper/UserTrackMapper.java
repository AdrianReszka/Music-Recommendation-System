package com.example.mapper;

import com.example.model.UserTrack;
import com.example.dto.UserTrackDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserTrackMapper {

    UserTrackDto toDto(UserTrack userTrack);
    UserTrack toEntity(UserTrackDto userTrackDto);
}