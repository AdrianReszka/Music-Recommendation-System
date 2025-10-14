package com.example.controller;

import com.example.dto.TrackDto;
import com.example.model.Track;
import com.example.service.LastFmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
    public ResponseEntity<?> fetchSimilarTracks(
            @RequestParam String username,
            @RequestParam String spotifyId,
            @RequestBody List<Long> trackIds) {
        try {
            List<TrackDto> recommendations = lastFmService.fetchSimilarTracksForUser(username, spotifyId, trackIds);
            return ResponseEntity.ok(recommendations);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));

        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
