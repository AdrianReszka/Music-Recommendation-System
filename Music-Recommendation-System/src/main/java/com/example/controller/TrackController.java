package com.example.controller;

import com.example.model.Track;
import com.example.repository.TrackRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/tracks")
public class TrackController {

    private final TrackRepository trackRepository;

    public TrackController(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @GetMapping
    public List<Track> getAll() {
        return trackRepository.findAll();
    }

    @GetMapping("/{id}")
    public Track getById(@PathVariable Long id) {
        return trackRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Track create(@RequestBody Track track) {
        return trackRepository.save(track);
    }

    @PutMapping("/{id}")
    public Track update(@PathVariable Long id, @RequestBody Track updated) {
        updated.setId(id);
        return trackRepository.save(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        trackRepository.deleteById(id);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from backend!"; }
}

