package com.example.service;

import com.example.model.Track;
import com.example.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;

    public List<Track> getAll() {
        return trackRepository.findAll();
    }

    public Track create(Track track) {
        return trackRepository.save(track);
    }

    public void delete(Long id) {
        trackRepository.deleteById(id);
    }
}

