package com.example.model;

import lombok.Data;

import java.util.List;

@Data
public class CreateSpotifyPlaylistRequest {
    private String name;
    private List<String> trackUris;
}
