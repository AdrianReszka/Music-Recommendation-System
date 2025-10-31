package com.example.app;

import com.example.dto.TagDto;
import com.example.dto.TrackDto;
import com.example.model.*;
import com.example.repository.*;
import com.example.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock private RecommendationRepository recommendationRepository;
    @Mock private UserRepository userRepository;
    @Mock private TrackRepository trackRepository;
    @Mock private SpotifyUserRepository spotifyUserRepository;
    @Mock private SpotifyUserLinkRepository spotifyUserLinkRepository;

    @InjectMocks private RecommendationService service;

    private User user;
    private Track track;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setLastfmUsername("test_user");

        track = new Track();
        track.setId(10L);
        track.setTitle("Song");
        track.setArtist("Artist");
    }

    @Test
    void deleteRecommendationBatch_shouldDeleteIfExists() {
        String batchId = "batch123";
        Recommendation rec = new Recommendation();
        rec.setUser(user);
        rec.setTrack(track);

        when(userRepository.findByLastfmUsername("test_user")).thenReturn(Optional.of(user));
        when(recommendationRepository.findByUserAndBatchId(user, batchId)).thenReturn(List.of(rec));

        service.deleteRecommendationBatch("test_user", batchId);

        verify(recommendationRepository).deleteAll(List.of(rec));
    }

    @Test
    void deleteRecommendationBatch_shouldThrowIfEmpty() {
        when(userRepository.findByLastfmUsername("test_user")).thenReturn(Optional.of(user));
        when(recommendationRepository.findByUserAndBatchId(any(), any())).thenReturn(List.of());

        assertThrows(RuntimeException.class, () ->
                service.deleteRecommendationBatch("test_user", "batch123"));
    }

    @Test
    void deleteRecommendationFromBatch_shouldCallRepositoryDelete() {
        service.deleteRecommendationFromBatch("test_user", "batch123", 5L);
        verify(recommendationRepository).deleteByUsernameAndBatchIdAndTrackId("test_user", "batch123", 5L);
    }

    @Test
    void saveRecommendations_shouldSaveForExistingTracks() {
        TrackDto dto = new TrackDto(null, "Song", "Artist", null, null, "lastfm", Set.of());
        when(userRepository.findByLastfmUsername("test_user")).thenReturn(Optional.of(user));
        when(trackRepository.findByTitleAndArtist("Song", "Artist")).thenReturn(Optional.of(track));

        service.saveRecommendations("test_user", List.of(dto));

        verify(recommendationRepository).save(any(Recommendation.class));
    }

    @Test
    void saveRecommendations_shouldSkipIfTrackNotFound() {
        TrackDto dto = new TrackDto(null, "Unknown", "Band", null, null, "lastfm", Set.of());
        when(userRepository.findByLastfmUsername("test_user")).thenReturn(Optional.of(user));
        when(trackRepository.findByTitleAndArtist("Unknown", "Band")).thenReturn(Optional.empty());

        service.saveRecommendations("test_user", List.of(dto));

        verify(recommendationRepository, never()).save(any());
    }

    @Test
    void getRecommendationsForUser_shouldReturnBatchListWhenBatchIdNull() {
        when(userRepository.findByLastfmUsername("test_user")).thenReturn(Optional.of(user));
        when(spotifyUserRepository.findBySpotifyId("spotify123")).thenReturn(Optional.of(new SpotifyUser()));
        when(recommendationRepository.findDistinctBatchIdsByUser(user.getId())).thenReturn(List.of(
                new Object[]{"batch001", LocalDateTime.of(2025, 1, 1, 12, 0)},
                new Object[]{"batch002", LocalDateTime.of(2025, 2, 2, 14, 30)}
        ));

        Object result = service.getRecommendationsForUser("test_user", "spotify123", null);

        assertThat(result).isInstanceOf(List.class);
        assertThat(((List<?>) result)).hasSize(2);
        verify(recommendationRepository).findDistinctBatchIdsByUser(user.getId());
    }

    @Test
    void getRecommendationsForUser_shouldReturnTrackDtosWhenBatchIdProvided() {
        String batchId = "batch001";
        Recommendation rec = new Recommendation();
        rec.setTrack(track);
        track.setTags(Set.of(new Tag(1L, "rock", new HashSet<>())));

        when(userRepository.findByLastfmUsername("test_user")).thenReturn(Optional.of(user));
        when(spotifyUserRepository.findBySpotifyId("spotify123")).thenReturn(Optional.of(new SpotifyUser()));
        when(recommendationRepository.findByUserAndBatchId(user, batchId)).thenReturn(List.of(rec));

        Object result = service.getRecommendationsForUser("test_user", "spotify123", batchId);

        assertThat(result).isInstanceOf(List.class);
        List<?> list = (List<?>) result;
        assertThat(list).hasSize(1);

        TrackDto dto = (TrackDto) list.get(0);
        assertThat(dto.getArtist()).isEqualTo("Artist");
        assertThat(dto.getTags()).extracting(TagDto::getName).contains("rock");
    }

    @Test
    void getRecommendationsForUser_shouldThrowIfUserNotFound() {
        when(userRepository.findByLastfmUsername("test_user")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                service.getRecommendationsForUser("test_user", "spotify123", null));
    }

    @Test
    void getRecommendationsForUser_shouldThrowIfSpotifyNotLinked() {
        when(userRepository.findByLastfmUsername("test_user")).thenReturn(Optional.of(user));
        when(spotifyUserRepository.findBySpotifyId("spotify123")).thenReturn(Optional.empty());
        assertThrows(SecurityException.class, () ->
                service.getRecommendationsForUser("test_user", "spotify123", null));
    }
}
