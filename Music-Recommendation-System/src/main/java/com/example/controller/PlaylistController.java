package com.example.controller;

import com.example.model.Playlist;
import com.example.service.PlaylistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public List<Playlist> getAllPlaylists() {
        return playlistService.getAll();
    }

    @GetMapping("/{id}")
    public Playlist getPlaylist(@PathVariable Long id) {
        return playlistService.getById(id);
    }

    @PostMapping
    public Playlist createPlaylist(@RequestBody Playlist playlist) {
        return playlistService.create(playlist);
    }

    @DeleteMapping("/{id}")
    public void deletePlaylist(@PathVariable Long id) {
        playlistService.delete(id);
    }
}
