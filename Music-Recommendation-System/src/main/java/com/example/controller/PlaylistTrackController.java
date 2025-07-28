package com.example.controller;

import com.example.model.PlaylistTrack;
import com.example.service.PlaylistTrackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/playlist-tracks")
public class PlaylistTrackController {

    private final PlaylistTrackService playlistTrackService;

    public PlaylistTrackController(PlaylistTrackService playlistTrackService) {
        this.playlistTrackService = playlistTrackService;
    }

    @GetMapping
    public List<PlaylistTrack> getAll() {
        return playlistTrackService.getAll();
    }

    @PostMapping
    public PlaylistTrack create(@RequestBody PlaylistTrack track) {
        return playlistTrackService.create(track);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        playlistTrackService.delete(id);
    }
}
