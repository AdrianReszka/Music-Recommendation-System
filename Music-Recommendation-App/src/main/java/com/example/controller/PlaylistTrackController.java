package com.example.controller;

import com.example.model.PlaylistTrack;
import com.example.repository.PlaylistTrackRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlist-tracks")
public class PlaylistTrackController {

    private final PlaylistTrackRepository playlistTrackRepository;

    public PlaylistTrackController(PlaylistTrackRepository playlistTrackRepository) {
        this.playlistTrackRepository = playlistTrackRepository;
    }

    @GetMapping
    public List<PlaylistTrack> getAll() {
        return playlistTrackRepository.findAll();
    }

    @GetMapping("/{id}")
    public PlaylistTrack getById(@PathVariable Long id) {
        return playlistTrackRepository.findById(id).orElse(null);
    }

    @PostMapping
    public PlaylistTrack create(@RequestBody PlaylistTrack entity) {
        return playlistTrackRepository.save(entity);
    }

    @PutMapping("/{id}")
    public PlaylistTrack update(@PathVariable Long id, @RequestBody PlaylistTrack updated) {
        updated.setId(id);
        return playlistTrackRepository.save(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        playlistTrackRepository.deleteById(id);
    }
}
