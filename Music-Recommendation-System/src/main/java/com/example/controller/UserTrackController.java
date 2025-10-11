package com.example.controller;

import com.example.dto.TrackDto;
import com.example.model.UserTrack;
import com.example.service.UserTrackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/musicapp/user-tracks")
public class UserTrackController {

    private final UserTrackService userTrackService;

    public UserTrackController(UserTrackService userTrackService) {
        this.userTrackService = userTrackService;
    }

    @GetMapping("/import")
    public ResponseEntity<String> importLovedTracks(@RequestParam String username, @RequestParam String spotifyId) {
        try {
            List<TrackDto> tracks = userTrackService.importLovedTracksFromLastFm(username, spotifyId);

            if (tracks.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("User exists but has no loved tracks");
            }

            return ResponseEntity.ok("Imported loved tracks for user: " + username);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found on Last.fm: " + username);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found on Last.fm: " + username);
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Error while calling Last.fm API");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import loved tracks");
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserTracks(@PathVariable String username, @RequestParam String spotifyId) {
        try {
            List<TrackDto> tracks = userTrackService.getTracksForUser(username, spotifyId);

            if (tracks.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("User exists but has no saved tracks");
            }

            return ResponseEntity.ok(tracks);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found: " + username);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch tracks: " + e.getMessage());
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
}

