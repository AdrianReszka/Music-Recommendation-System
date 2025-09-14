package com.example.controller;

import com.example.dto.TrackDto;
import com.example.model.UserTrack;
import com.example.service.UserTrackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/user-tracks")
public class UserTrackController {

    private final UserTrackService userTrackService;

    public UserTrackController(UserTrackService userTrackService) {
        this.userTrackService = userTrackService;
    }

    @GetMapping("/import")
    public ResponseEntity<String> importLovedTracks(@RequestParam String username) {
        try {
            userTrackService.importLovedTracksFromLastFm(username);
            return ResponseEntity.ok("Imported loved tracks for user: " + username);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import loved tracks: " + e.getMessage());
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<TrackDto>> getUserTracks(@PathVariable String username) {
        try {
            List<TrackDto> tracks = userTrackService.getTracksForUser(username);
            return ResponseEntity.ok(tracks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(List.of());
        }
    }

    @GetMapping
    public List<UserTrack> getAll() {
        return userTrackService.getAll();
    }

    @PostMapping
    public UserTrack create(@RequestBody UserTrack track) {
        return userTrackService.create(track);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userTrackService.delete(id);
    }
}

