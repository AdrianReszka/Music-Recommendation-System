package com.example.mapper;

import com.example.model.PlaylistTrack;
import com.example.dto.PlaylistTrackDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlaylistTrackMapper {

    PlaylistTrackDto toDto(PlaylistTrack playlistTrack);
    PlaylistTrack toEntity(PlaylistTrackDto playlistTrackDto);
}