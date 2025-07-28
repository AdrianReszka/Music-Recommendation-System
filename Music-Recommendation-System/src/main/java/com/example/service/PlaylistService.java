package com.example.service;

import com.example.model.Playlist;
import com.example.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public List<Playlist> getAll() {
        return playlistRepository.findAll();
    }

    public Playlist getById(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
    }

    public Playlist create(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public void delete(Long id) {
        playlistRepository.deleteById(id);
    }
}

