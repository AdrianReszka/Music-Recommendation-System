package com.example.app;

import com.example.dto.TrackDto;
import com.example.model.*;
import com.example.repository.*;
import com.example.service.LastFmService;
import com.example.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastFmServiceTest {

    @Mock private StatsService statsService;
    @Mock private TrackRepository trackRepository;
    @Mock private UserRepository userRepository;
    @Mock private SpotifyUserRepository spotifyUserRepository;
    @Mock private SpotifyUserLinkRepository spotifyUserLinkRepository;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private LastFmService service;

    private final String username = "test_user";
    private final String spotifyId = "sp123";
    private User user;

    @BeforeEach
    void setup() throws Exception {
        var apiField = LastFmService.class.getDeclaredField("apiKey");
        apiField.setAccessible(true);
        apiField.set(service, "fake-key");

        var restField = LastFmService.class.getDeclaredField("restTemplate");
        restField.setAccessible(true);
        restField.set(service, restTemplate);

        user = new User();
        user.setId(1L);
        user.setLastfmUsername(username);
    }

    @Test
    void fetchLovedTracks_shouldThrowWhenNoLovedTracksField() {
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of()));

        assertThrows(NoSuchElementException.class,
                () -> service.fetchLovedTracks(username, spotifyId));
    }

    @Test
    void fetchLovedTracks_shouldReturnEmptyListWhenTrackListEmpty() {
        Map<String, Object> loved = Map.of("track", Collections.emptyList());
        Map<String, Object> body = Map.of("lovedtracks", loved);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        List<TrackDto> result = service.fetchLovedTracks(username, spotifyId);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchLovedTracks_shouldCreateUserIfNotExistsAndSaveTracks() {
        Map<String, Object> artistMap = Map.of("name", "Artist");
        Map<String, Object> trackMap = new HashMap<>();
        trackMap.put("name", "Song");
        trackMap.put("artist", artistMap);
        trackMap.put("mbid", "abc123");

        Map<String, Object> loved = Map.of("track", List.of(trackMap));
        Map<String, Object> body = Map.of("lovedtracks", loved);

        when(restTemplate.getForEntity(startsWith("https://ws.audioscrobbler.com/2.0/"), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        when(userRepository.findByLastfmUsername(username)).thenReturn(Optional.empty());
        when(trackRepository.findByTitleAndArtist("Song", "Artist")).thenReturn(Optional.empty());

        SpotifyUser spotifyUser = new SpotifyUser();
        spotifyUser.setSpotifyId(spotifyId);
        when(spotifyUserRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(spotifyUser));
        when(spotifyUserLinkRepository.existsBySpotifyUserAndUser(any(), any())).thenReturn(false);

        service.fetchLovedTracks(username, spotifyId);

        verify(userRepository).save(any(User.class));
        verify(trackRepository).saveAll(anyList());
        verify(statsService, atLeastOnce()).updateIfIncreased();
    }

    @Test
    void fetchSimilarTracksForUser_shouldThrowIfSpotifyUserMissing() {
        when(spotifyUserRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.empty());

        assertThrows(SecurityException.class,
                () -> service.fetchSimilarTracksForUser(username, spotifyId, List.of(1L)));
    }

    @Test
    void fetchSimilarTracksForUser_shouldThrowIfLinkNotExists() {
        SpotifyUser sUser = new SpotifyUser();
        sUser.setSpotifyId(spotifyId);
        when(spotifyUserRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(sUser));

        when(userRepository.findByLastfmUsername(username)).thenReturn(Optional.of(user));
        when(spotifyUserLinkRepository.existsBySpotifyUserAndUser(sUser, user)).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> service.fetchSimilarTracksForUser(username, spotifyId, List.of(1L)));
    }

    @Test
    void fetchSimilarTracksForUser_shouldThrowIfNoRecommendationsFound() {
        SpotifyUser sUser = new SpotifyUser();
        sUser.setSpotifyId(spotifyId);
        sUser.setAccessToken("tok");
        when(spotifyUserRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(sUser));
        when(userRepository.findByLastfmUsername(username)).thenReturn(Optional.of(user));
        when(spotifyUserLinkRepository.existsBySpotifyUserAndUser(sUser, user)).thenReturn(true);

        Map<String, Object> sim = Map.of("track", Collections.emptyList());
        Map<String, Object> body = Map.of("similartracks", sim);
        when(restTemplate.getForEntity(contains("getsimilar"), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        Track t = new Track();
        t.setId(1L);
        t.setTitle("Song");
        t.setArtist("Artist");

        when(trackRepository.findAllWithTagsByIdIn(List.of(1L))).thenReturn(List.of(t));

        assertThrows(NoSuchElementException.class,
                () -> service.fetchSimilarTracksForUser(username, spotifyId, List.of(1L)));
    }
}
