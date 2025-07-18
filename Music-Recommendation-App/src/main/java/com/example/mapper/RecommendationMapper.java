package com.example.mapper;

import com.example.model.Recommendation;
import com.example.dto.RecommendationDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    RecommendationDto toDto(Recommendation recommendation);
    Recommendation toEntity(RecommendationDto recommendationDto);
}