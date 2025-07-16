package com.example.controller;

import com.example.model.UserTrack;
import com.example.repository.UserTrackRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-tracks")
public class UserTrackController {

    private final UserTrackRepository userTrackRepository;

    public UserTrackController(UserTrackRepository userTrackRepository) {
        this.userTrackRepository = userTrackRepository;
    }

    @GetMapping
    public List<UserTrack> getAll() {
        return userTrackRepository.findAll();
    }

    @GetMapping("/{id}")
    public UserTrack getById(@PathVariable Long id) {
        return userTrackRepository.findById(id).orElse(null);
    }

    @PostMapping
    public UserTrack create(@RequestBody UserTrack entity) {
        return userTrackRepository.save(entity);
    }

    @PutMapping("/{id}")
    public UserTrack update(@PathVariable Long id, @RequestBody UserTrack updated) {
        updated.setId(id);
        return userTrackRepository.save(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userTrackRepository.deleteById(id);
    }
}
