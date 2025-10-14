package com.example.service;

import com.example.dto.TagDto;
import com.example.dto.TrackDto;
import com.example.model.Recommendation;
import com.example.model.SpotifyUser;
import com.example.model.Track;
import com.example.model.User;
import com.example.repository.RecommendationRepository;
import com.example.repository.UserRepository;
import com.example.repository.SpotifyUserLinkRepository;
import com.example.repository.SpotifyUserRepository;
import com.example.repository.TrackRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final SpotifyUserRepository spotifyUserRepository;
    private final SpotifyUserLinkRepository spotifyUserLinkRepository;

    public List<Recommendation> getAll() {
        return recommendationRepository.findAll();
    }

    public Recommendation create(Recommendation recommendation) {
        return recommendationRepository.save(recommendation);
    }

    @Transactional
    public void deleteRecommendationBatch(String username, String batchId) {
        User user = userRepository.findByLastfmUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<Recommendation> recs = recommendationRepository.findByUserAndBatchId(user, batchId);

        if (recs.isEmpty()) {
            throw new RuntimeException("No recommendations found for batch " + batchId);
        }

        recommendationRepository.deleteAll(recs);
    }

    @Transactional
    public void deleteRecommendationFromBatch(String username, String batchId, Long trackId) {
        recommendationRepository.deleteByUsernameAndBatchIdAndTrackId(username, batchId, trackId);
    }

    public void saveRecommendations(String username, List<TrackDto> tracks) {
        User user = userRepository.findByLastfmUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (TrackDto dto : tracks) {
            Optional<Track> trackOpt = trackRepository.findByTitleAndArtist(dto.getTitle(), dto.getArtist());
            trackOpt.ifPresent(track -> {

                Recommendation rec = new Recommendation();
                rec.setUser(user);
                rec.setTrack(track);
                recommendationRepository.save(rec);
            });
        }
    }

    public Object getRecommendationsForUser(String username, String spotifyId, String batchId) {
        User user = userRepository.findByLastfmUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        SpotifyUser spotifyUser = spotifyUserRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new SecurityException("Spotify account not found"));

        if (batchId == null || batchId.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            return recommendationRepository.findDistinctBatchIdsByUser(user.getId())
                    .stream()
                    .map(obj -> {
                        String batchIdStr = (String) obj[0];
                        LocalDateTime createdAt = (LocalDateTime) obj[1];

                        String formattedDate = createdAt != null ? createdAt.format(formatter) : "unknown";

                        return Map.of(
                                "batchId", batchIdStr,
                                "createdAt", formattedDate
                        );
                    })
                    .toList();
        }

        List<Recommendation> recommendations = recommendationRepository.findByUserAndBatchId(user, batchId);

        return recommendations.stream()
                .map(rec -> {
                    Track t = rec.getTrack();
                    return new TrackDto(
                            t.getId(),
                            t.getTitle(),
                            t.getArtist(),
                            t.getSpotifyId(),
                            t.getLastfmId(),
                            t.getSource(),
                            t.getTags().stream()
                                    .map(tag -> {
                                TagDto dto = new TagDto();
                                dto.setName(tag.getName());
                                return dto;
                            }).collect(Collectors.toSet())
                    );
                })
                .toList();
    }
}

