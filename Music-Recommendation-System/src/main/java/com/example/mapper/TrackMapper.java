package com.example.mapper;

import com.example.model.Track;
import com.example.dto.TrackDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrackMapper {

    TrackDto toDto(Track track);
    Track toEntity(TrackDto trackDto);
}