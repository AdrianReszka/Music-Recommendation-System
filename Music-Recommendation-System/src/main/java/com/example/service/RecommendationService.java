package com.example.service;

import com.example.dto.TagDto;
import com.example.dto.TrackDto;
import com.example.model.Recommendation;
import com.example.model.Track;
import com.example.model.User;
import com.example.repository.RecommendationRepository;
import com.example.repository.UserRepository;
import com.example.repository.SpotifyUserLinkRepository;
import com.example.repository.SpotifyUserRepository;
import com.example.repository.TrackRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void delete(Long id) {
        recommendationRepository.deleteById(id);
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

    public List<TrackDto> getRecommendationsForUser(String username, String spotifyId, String batchId) {
        User user = userRepository.findByLastfmUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (spotifyId == null || spotifyId.isEmpty()) {
            throw new RuntimeException("Spotify ID is required");
        }

        var su = spotifyUserRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new SecurityException("Spotify account not found"));
        boolean isLinked = spotifyUserLinkRepository.existsBySpotifyUserAndUser(su, user);

        if (!isLinked) {
            throw new SecurityException("This Last.fm user is not linked with your Spotify account");
        }

        List<Recommendation> recommendations;
        if (batchId != null && !batchId.isEmpty()) {
            recommendations = recommendationRepository.findByUserAndBatchId(user, batchId);
        } else {
            recommendations = recommendationRepository.findByUser(user);
        }

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
                            t.getTags().stream().map(tag -> {
                                TagDto dto = new TagDto();
                                dto.setName(tag.getName());
                                return dto;
                            }).collect(Collectors.toSet())
                    );
                })
                .toList();
    }
}

