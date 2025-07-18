package com.example.controller;

import com.example.model.Playlist;
import com.example.repository.PlaylistRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/playlists")
public class PlaylistController {

    private final PlaylistRepository playlistRepository;

    public PlaylistController(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    @GetMapping
    public List<Playlist> getAll() {
        return playlistRepository.findAll();
    }

    @GetMapping("/{id}")
    public Playlist getById(@PathVariable Long id) {
        return playlistRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Playlist create(@RequestBody Playlist entity) {
        return playlistRepository.save(entity);
    }

    @PutMapping("/{id}")
    public Playlist update(@PathVariable Long id, @RequestBody Playlist updated) {
        updated.setId(id);
        return playlistRepository.save(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        playlistRepository.deleteById(id);
    }
}
