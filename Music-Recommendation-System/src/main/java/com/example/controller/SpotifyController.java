package com.example.controller;

import com.example.dto.TrackDto;
import com.example.repository.SpotifyUserRepository;
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
    private final SpotifyUserRepository spotifyUserRepository;

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String loginUrl = spotifyService.buildLoginUrl();
        response.sendRedirect(loginUrl);
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
        String accessToken = spotifyService.exchangeCodeForAccessToken(code);

        var userInfo = spotifyService.getSpotifyUserInfo(accessToken);
        String spotifyId = userInfo.get("id");
        String displayName = userInfo.get("display_name");

        spotifyService.saveOrUpdateSpotifyUser(spotifyId, displayName, accessToken);

        response.sendRedirect("https://beatbridge-c4hbh6bgcjdggra5.polandcentral-01.azurewebsites.net/callback"
                + "?spotifyId=" + URLEncoder.encode(spotifyId, StandardCharsets.UTF_8)
                + "&username=" + URLEncoder.encode(displayName, StandardCharsets.UTF_8));
    }

    @PostMapping("/save-playlist")
    public ResponseEntity<String> savePlaylist(@RequestParam String spotifyId, @RequestBody List<TrackDto> tracks) {
        System.out.println("Received Spotify ID: " + spotifyId);

        if (spotifyId == null || spotifyId.isEmpty()) {
            return ResponseEntity.badRequest().body("Spotify ID is missing");
        }

        try {
            spotifyService.createPlaylistWithTracks(spotifyId, tracks);
            return ResponseEntity.ok("Playlist created and tracks added successfully!");
        } catch (Exception e) {
            System.err.println("Error creating playlist: " + e.getMessage());
            return ResponseEntity.status(500).body("Error creating playlist");
        }
    }
}
