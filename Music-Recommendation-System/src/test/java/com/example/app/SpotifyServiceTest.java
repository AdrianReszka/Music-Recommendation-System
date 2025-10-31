package com.example.app;

import com.example.dto.TrackDto;
import com.example.model.SpotifyUser;
import com.example.repository.SpotifyUserRepository;
import com.example.service.SpotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyServiceTest {

    @Mock private SpotifyUserRepository spotifyUserRepository;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private SpotifyService service;

    private final String clientId = "client123";
    private final String clientSecret = "secret456";
    private final String redirectUri = "http://localhost/callback";
    private final String accessToken = "token123";

    @BeforeEach
    void setup() throws Exception {
        var f1 = SpotifyService.class.getDeclaredField("clientId");
        f1.setAccessible(true);
        f1.set(service, clientId);

        var f2 = SpotifyService.class.getDeclaredField("clientSecret");
        f2.setAccessible(true);
        f2.set(service, clientSecret);

        var f3 = SpotifyService.class.getDeclaredField("redirectUri");
        f3.setAccessible(true);
        f3.set(service, redirectUri);

        var f4 = SpotifyService.class.getDeclaredField("restTemplate");
        f4.setAccessible(true);
        f4.set(service, restTemplate);
    }

    @Test
    void buildLoginUrl_shouldContainClientIdAndRedirect() {
        String url = service.buildLoginUrl();

        assertTrue(url.contains("client_id=" + clientId));
        assertTrue(url.contains("redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)));
        assertTrue(url.contains("authorize"));
    }

    @Test
    void exchangeCodeForAccessToken_shouldReturnTokenWhenPresent() {
        Map<String, Object> body = Map.of("access_token", "abc123");
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        String token = service.exchangeCodeForAccessToken("code123");
        assertEquals("abc123", token);
    }

    @Test
    void exchangeCodeForAccessToken_shouldThrowIfMissingAccessToken() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("other", "value")));

        assertThrows(RuntimeException.class,
                () -> service.exchangeCodeForAccessToken("code123"));
    }

    @Test
    void getSpotifyUserInfo_shouldReturnBasicFields() {
        Map<String, Object> body = new HashMap<>();
        body.put("id", "sp123");
        body.put("display_name", "test_user");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        Map<String, String> info = service.getSpotifyUserInfo(accessToken);
        assertEquals("sp123", info.get("id"));
        assertEquals("test_user", info.get("display_name"));
    }

    @Test
    void getSpotifyUserInfo_shouldThrowIfBodyNull() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThrows(RuntimeException.class,
                () -> service.getSpotifyUserInfo(accessToken));
    }

    @Test
    void saveOrUpdateSpotifyUser_shouldCreateNewUserIfNotExists() {
        when(spotifyUserRepository.findBySpotifyId("sp123")).thenReturn(Optional.empty());

        service.saveOrUpdateSpotifyUser("sp123", "test_user1", accessToken);

        verify(spotifyUserRepository).save(any(SpotifyUser.class));
    }

    @Test
    void saveOrUpdateSpotifyUser_shouldThrowIfIdOrTokenNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveOrUpdateSpotifyUser(null, "test_user", accessToken));
        assertThrows(IllegalArgumentException.class,
                () -> service.saveOrUpdateSpotifyUser("sp123", "test_user1", null));
    }

    @Test
    void searchSpotifyTrack_shouldReturnTrackUriWhenFound() {
        Map<String, Object> trackItem = Map.of("id", "t1");
        Map<String, Object> tracks = Map.of("items", List.of(trackItem));
        Map<String, Object> body = Map.of("tracks", tracks);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        String uri = service.searchSpotifyTrack(accessToken, "Song", "Artist");
        assertEquals("spotify:track:t1", uri);
    }

    @Test
    void searchSpotifyTrack_shouldReturnNullWhenEmptyItems() {
        Map<String, Object> tracks = Map.of("items", Collections.emptyList());
        Map<String, Object> body = Map.of("tracks", tracks);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        assertNull(service.searchSpotifyTrack(accessToken, "Song", "Artist"));
    }

    @Test
    void searchSpotifyTrack_shouldReturnNullWhenError() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Network fail"));
        assertNull(service.searchSpotifyTrack(accessToken, "Song", "Artist"));
    }

    @Test
    void createPlaylistWithTracks_shouldThrowIfUserNotFound() {
        when(spotifyUserRepository.findBySpotifyId("sp123")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.createPlaylistWithTracks("sp123", List.of()));
    }

    @Test
    void createPlaylistWithTracks_shouldThrowIfNoTracksAdded() {
        SpotifyUser user = new SpotifyUser();
        user.setSpotifyId("sp123");
        user.setAccessToken(accessToken);

        when(spotifyUserRepository.findBySpotifyId("sp123")).thenReturn(Optional.of(user));

        Map<String, Object> responseBody = Map.of("id", "playlist1");
        when(restTemplate.postForEntity(contains("/playlists"), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        SpotifyService spyService = spy(service);
        doReturn(null).when(spyService).searchSpotifyTrack(anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class,
                () -> spyService.createPlaylistWithTracks("sp123", List.of(
                        new TrackDto(1L, "Song", "Artist", null, null, null, Set.of())
                )));
    }

    @Test
    void createPlaylistWithTracks_shouldAddTracksWhenFound() {
        SpotifyUser user = new SpotifyUser();
        user.setSpotifyId("sp123");
        user.setAccessToken(accessToken);
        when(spotifyUserRepository.findBySpotifyId("sp123")).thenReturn(Optional.of(user));

        Map<String, Object> playlistResponse = Map.of("id", "pl123");
        when(restTemplate.postForEntity(contains("/playlists"), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(playlistResponse));

        SpotifyService spyService = spy(service);
        doReturn("spotify:track:abc").when(spyService)
                .searchSpotifyTrack(anyString(), anyString(), anyString());

        spyService.createPlaylistWithTracks("sp123", List.of(
                new TrackDto(1L, "Song", "Artist", null, null, null, Set.of())
        ));

        verify(restTemplate).postForEntity(contains("/tracks"), any(), eq(Void.class));
    }
}
