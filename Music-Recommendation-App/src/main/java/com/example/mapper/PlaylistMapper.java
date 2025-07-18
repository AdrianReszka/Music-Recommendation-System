package com.example.mapper;

import com.example.model.Playlist;
import com.example.dto.PlaylistDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlaylistMapper {

    PlaylistDto toDto(Playlist playlist);
    Playlist toEntity(PlaylistDto playlistDto);
}