package com.example.controller;

import com.example.model.SpotifyUser;
import com.example.service.SpotifyService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/musicapp/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String loginUrl = spotifyService.buildLoginUrl();
        response.sendRedirect(loginUrl);
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
        SpotifyUser user = spotifyService.exchangeCodeAndSaveUser(code);
        response.sendRedirect("http://localhost:5173/spotify-callback?username=" + URLEncoder.encode(user.getSpotifyId(), StandardCharsets.UTF_8));
    }

    @PostMapping("/save-playlist")
    public ResponseEntity<Void> savePlaylist(@RequestParam String username, @RequestBody List<String> trackUris) {
        spotifyService.createPlaylistWithTracks(username, trackUris);
        return ResponseEntity.ok().build();
    }
}
