package com.example.app;

import com.example.dto.TrackDto;
import com.example.model.*;
import com.example.repository.*;
import com.example.service.LastFmService;
import com.example.service.UserTrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTrackServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserTrackRepository userTrackRepository;
    @Mock private TrackRepository trackRepository;
    @Mock private LastFmService lastFmService;
    @Mock private SpotifyUserRepository spotifyUserRepository;
    @Mock private SpotifyUserLinkRepository spotifyUserLinkRepository;

    @InjectMocks
    private UserTrackService service;

    private final String username = "testUser";
    private final String spotifyId = "spotify123";

    private TrackDto sampleTrackDto;
    private Track sampleTrack;
    private User sampleUser;
    private SpotifyUser sampleSpotifyUser;

    @BeforeEach
    void setup() {
        sampleTrackDto = new TrackDto(1L, "Song", "Artist", "spId", "lfmId", "lastfm", Set.of());

        sampleTrack = new Track();
        sampleTrack.setId(1L);
        sampleTrack.setTitle("Song");
        sampleTrack.setArtist("Artist");

        Tag tag = new Tag();
        tag.setName("rock");
        sampleTrack.setTags(Set.of(tag));

        sampleUser = new User();
        sampleUser.setId(100L);
        sampleUser.setLastfmUsername(username);

        sampleSpotifyUser = new SpotifyUser();
        sampleSpotifyUser.setSpotifyId(spotifyId);
    }

    @Test
    void importLovedTracks_shouldReturnEmptyListWhenNoLovedTracks() {
        when(lastFmService.fetchLovedTracks(username, spotifyId)).thenReturn(Collections.emptyList());
        var result = service.importLovedTracksFromLastFm(username, spotifyId);
        assertTrue(result.isEmpty());
        verifyNoInteractions(userRepository, userTrackRepository, trackRepository);
    }

    @Test
    void importLovedTracks_shouldCreateNewUserWhenNotExists() {
        when(lastFmService.fetchLovedTracks(username, spotifyId)).thenReturn(List.of(sampleTrackDto));
        when(userRepository.findByLastfmUsername(username)).thenReturn(Optional.empty());
        when(trackRepository.findByTitleAndArtist("Song", "Artist")).thenReturn(Optional.of(sampleTrack));
        when(spotifyUserRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(sampleSpotifyUser));
        when(spotifyUserLinkRepository.existsBySpotifyUserAndUser(any(), any())).thenReturn(false);
        when(userTrackRepository.existsByUserAndTrack(any(), any())).thenReturn(false);

        service.importLovedTracksFromLastFm(username, spotifyId);

        verify(userRepository).save(any(User.class));
        verify(spotifyUserLinkRepository).save(any(SpotifyUserLink.class));
        verify(userTrackRepository).save(any(UserTrack.class));
    }

    @Test
    void importLovedTracks_shouldThrowIfSpotifyUserNotFound() {
        when(lastFmService.fetchLovedTracks(username, spotifyId)).thenReturn(List.of(sampleTrackDto));
        when(userRepository.findByLastfmUsername(username)).thenReturn(Optional.of(sampleUser));
        when(spotifyUserRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.importLovedTracksFromLastFm(username, spotifyId));

        assertTrue(ex.getMessage().contains("Spotify user not found"));
    }

    @Test
    void importLovedTracks_shouldThrowIfTrackMissingInRepo() {
        when(lastFmService.fetchLovedTracks(username, null)).thenReturn(List.of(sampleTrackDto));
        when(userRepository.findByLastfmUsername(username)).thenReturn(Optional.of(sampleUser));
        when(trackRepository.findByTitleAndArtist("Song", "Artist")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.importLovedTracksFromLastFm(username, null));

        assertTrue(ex.getMessage().contains("Track not found"));
    }


    @Test
    void getTracksForUser_shouldReturnMappedDtos() {
        UserTrack ut = new UserTrack();
        ut.setUser(sampleUser);
        ut.setTrack(sampleTrack);

        when(userRepository.findByLastfmUsername(username)).thenReturn(Optional.of(sampleUser));
        when(spotifyUserRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(sampleSpotifyUser));
        when(spotifyUserLinkRepository.existsBySpotifyUserAndUser(sampleSpotifyUser, sampleUser)).thenReturn(true);
        when(userTrackRepository.findByUser(sampleUser)).thenReturn(List.of(ut));

        var result = service.getTracksForUser(username, spotifyId);

        assertEquals(1, result.size());
        assertEquals("Song", result.get(0).getTitle());
        assertEquals("Artist", result.get(0).getArtist());
    }

    @Test
    void getTracksForUser_shouldThrowWhenUserNotFound() {
        when(userRepository.findByLastfmUsername(username)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.getTracksForUser(username, spotifyId));
    }

    @Test
    void getTracksForUser_shouldThrowWhenSpotifyNotLinked() {
        when(userRepository.findByLastfmUsername(username)).thenReturn(Optional.of(sampleUser));
        when(spotifyUserRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(sampleSpotifyUser));
        when(spotifyUserLinkRepository.existsBySpotifyUserAndUser(sampleSpotifyUser, sampleUser)).thenReturn(false);

        assertThrows(SecurityException.class, () -> service.getTracksForUser(username, spotifyId));
    }
}
