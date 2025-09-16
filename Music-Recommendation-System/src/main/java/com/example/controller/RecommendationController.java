package com.example.controller;

import com.example.dto.TrackDto;
import com.example.model.Recommendation;
import com.example.service.RecommendationService;
import com.example.repository.RecommendationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RecommendationRepository recommendationRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    RecommendationRepository recommendationRepository) {

        this.recommendationService = recommendationService;
        this.recommendationRepository = recommendationRepository;

    }

    @GetMapping
    public List<Recommendation> getAllRecommendations() {
        return recommendationService.getAll();
    }

    @PostMapping
    public Recommendation createRecommendation(@RequestBody Recommendation recommendation) {
        return recommendationService.create(recommendation);
    }

    @DeleteMapping("/{id}")
    public void deleteRecommendation(@PathVariable Long id) {
        recommendationService.delete(id);
    }

    @GetMapping("/users")
    public List<String> getUsersWithRecommendations() {
        return recommendationRepository.findDistinctUsernames();
    }

    @GetMapping("/user/{username}")
    public List<TrackDto> getRecommendationsForUser(@PathVariable String username) {
        return recommendationService.getRecommendationsForUser(username);
    }
}

