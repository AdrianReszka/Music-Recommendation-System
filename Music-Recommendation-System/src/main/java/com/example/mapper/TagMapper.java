package com.example.mapper;

import com.example.model.Tag;
import com.example.dto.TagDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagDto toDto(Tag tag);
    Tag toEntity(TagDto tagDto);
}