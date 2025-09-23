package com.example.service;

import com.example.dto.TrackDto;
import com.example.model.SpotifyUser;
import com.example.repository.SpotifyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final SpotifyUserRepository spotifyUserRepository;

    private static final String FIXED_PLAYLIST_NAME = "BeatBridge Recommendations Playlist";

    public String buildLoginUrl() {
        return "https://accounts.spotify.com/authorize" +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=playlist-modify-public playlist-modify-private user-read-private";
    }

    public String exchangeCodeForAccessToken(String code) {
        String url = "https://accounts.spotify.com/api/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map body = response.getBody();

        if (body == null || !body.containsKey("access_token")) {
            throw new RuntimeException("No access token returned from Spotify");
        }

        return (String) body.get("access_token");
    }

    public Map<String, String> getSpotifyUserInfo(String accessToken) {
        String url = "https://api.spotify.com/v1/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        Map body = response.getBody();
        if (body == null) throw new RuntimeException("Spotify user info not found");

        return Map.of(
                "id", (String) body.get("id"),
                "display_name", (String) body.getOrDefault("display_name", "Unknown")
        );
    }

    public void saveOrUpdateSpotifyUser(String spotifyId, String displayName, String accessToken) {
        SpotifyUser user = spotifyUserRepository.findBySpotifyId(spotifyId)
                .orElseGet(SpotifyUser::new);

        user.setSpotifyId(spotifyId);
        user.setDisplayName(displayName);
        user.setAccessToken(accessToken);

        spotifyUserRepository.save(user);
    }

    private String searchSpotifyTrack(String accessToken, String title, String artist) {
        try {
            String query = URLEncoder.encode(title + " " + artist, StandardCharsets.UTF_8);
            String url = "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=1";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Map body = response.getBody();
            if (body == null || !body.containsKey("tracks")) {
                return null;
            }

            Map<String, Object> tracks = (Map<String, Object>) body.get("tracks");
            List<Map<String, Object>> items = (List<Map<String, Object>>) tracks.get("items");

            if (items == null || items.isEmpty()) {
                return null;
            }

            String id = (String) items.get(0).get("id");
            return "spotify:track:" + id;
        } catch (Exception e) {
            System.err.println("Failed to search track: " + title + " - " + artist);
            return null;
        }
    }

    public void createPlaylistWithTracks(String spotifyId, List<TrackDto> tracks) {
        SpotifyUser user = spotifyUserRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new RuntimeException("Spotify user not found"));

        String accessToken = user.getAccessToken();

        String createPlaylistUrl = "https://api.spotify.com/v1/users/" + user.getSpotifyId() + "/playlists";

        Map<String, Object> body = new HashMap<>();
        body.put("name", FIXED_PLAYLIST_NAME);
        body.put("description", "Generated by BeatBridge");
        body.put("public", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(createPlaylistUrl, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to create playlist");
        }

        String playlistId = (String) response.getBody().get("id");

        List<String> trackUris = tracks.stream()
                .map(t -> searchSpotifyTrack(accessToken, t.getTitle(), t.getArtist()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (trackUris.isEmpty()) {
            throw new RuntimeException("No tracks could be mapped to Spotify URIs");
        }

        String addTracksUrl = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";
        Map<String, Object> tracksBody = Map.of("uris", trackUris);

        System.out.println("Dodaję do playlisty " + FIXED_PLAYLIST_NAME + " utwory: " + trackUris);

        restTemplate.postForEntity(addTracksUrl, new HttpEntity<>(tracksBody, headers), Void.class);
    }
}
