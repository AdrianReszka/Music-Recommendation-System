package com.example.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackDto {

    private String title;
    private String artist;
    private String spotifyId;
    private String lastfmId;
    private String source;
}