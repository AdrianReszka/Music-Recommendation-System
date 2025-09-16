package com.example.service;

import com.example.dto.TrackDto;
import com.example.model.Recommendation;
import com.example.model.Track;
import com.example.model.User;
import com.example.repository.RecommendationRepository;
import com.example.repository.UserRepository;
import com.example.repository.TrackRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;

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
}

