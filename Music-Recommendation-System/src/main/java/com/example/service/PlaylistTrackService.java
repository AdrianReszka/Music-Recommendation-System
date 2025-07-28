package com.example.service;

import com.example.model.PlaylistTrack;
import com.example.repository.PlaylistTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistTrackService {

    private final PlaylistTrackRepository playlistTrackRepository;

    public List<PlaylistTrack> getAll() {
        return playlistTrackRepository.findAll();
    }

    public PlaylistTrack create(PlaylistTrack track) {
        return playlistTrackRepository.save(track);
    }

    public void delete(Long id) {
        playlistTrackRepository.deleteById(id);
    }
}

