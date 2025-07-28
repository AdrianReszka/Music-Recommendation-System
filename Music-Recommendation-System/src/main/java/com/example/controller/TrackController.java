package com.example.controller;

import com.example.model.Track;
import com.example.service.TrackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping
    public List<Track> getAllTracks() {
        return trackService.getAll();
    }

    @PostMapping
    public Track createTrack(@RequestBody Track track) {
        return trackService.create(track);
    }

    @DeleteMapping("/{id}")
    public void deleteTrack(@PathVariable Long id) {
        trackService.delete(id);
    }
}

