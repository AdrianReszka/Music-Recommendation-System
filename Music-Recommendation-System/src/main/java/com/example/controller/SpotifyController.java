package com.example.controller;

import com.example.model.CreateSpotifyPlaylistRequest;
import com.example.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/musicapp/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam("userId") Integer userId) {
        URI redirectUri = spotifyService.buildAuthorizationUri(userId);
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam String code,
                                           @RequestParam Long state) {
        spotifyService.exchangeCodeForToken(code, state);
        return ResponseEntity.ok("Spotify login successful!");
    }

    @PostMapping("/playlist")
    public ResponseEntity<Void> createPlaylist(@RequestParam("userId") Long userId,
                                               @RequestBody CreateSpotifyPlaylistRequest request) {
        spotifyService.createPlaylistForUser(request, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<String> getSpotifyDisplayName(@RequestParam("userId") Long userId) {
        String displayName = spotifyService.getDisplayName(userId);
        return ResponseEntity.ok(displayName);
    }

}
