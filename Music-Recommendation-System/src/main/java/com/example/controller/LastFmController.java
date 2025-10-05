package com.example.controller;

import com.example.dto.TrackDto;
import com.example.model.Track;
import com.example.service.LastFmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/lastfm")
public class LastFmController {

    private final LastFmService lastFmService;

    public LastFmController(LastFmService lastFmService) {
        this.lastFmService = lastFmService;
    }

    @GetMapping("/loved")
    public ResponseEntity<List<TrackDto>> getLovedTracks(@RequestParam String username, @RequestParam String spotifyId) {
        List<TrackDto> lovedTracks = lastFmService.fetchLovedTracks(username, spotifyId);
        return ResponseEntity.ok(lovedTracks);
    }

    @PostMapping("/similar")
    public ResponseEntity<List<TrackDto>> getSimilarTracks(
            @RequestParam String username,
            @RequestBody List<Long> selectedTrackIds
    ) {
        List<TrackDto> result = lastFmService.fetchSimilarTracksForUser(username, selectedTrackIds);
        return ResponseEntity.ok(result);
    }


}