package com.example.dto;

import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrackDto {
    private Long id;
    private String title;
    private String artist;
    private String spotifyId;
    private String lastfmId;
    private String source;
    private Set<TagDto> tags;
}
