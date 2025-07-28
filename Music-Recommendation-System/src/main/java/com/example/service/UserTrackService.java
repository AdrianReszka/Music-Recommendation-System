package com.example.service;

import com.example.model.UserTrack;
import com.example.repository.UserTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTrackService {

    private final UserTrackRepository userTrackRepository;

    public List<UserTrack> getAll() {
        return userTrackRepository.findAll();
    }

    public UserTrack create(UserTrack track) {
        return userTrackRepository.save(track);
    }

    public void delete(Long id) {
        userTrackRepository.deleteById(id);
    }
}

