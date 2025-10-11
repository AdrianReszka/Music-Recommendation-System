package com.example.controller;

import com.example.dto.TrackDto;
import com.example.model.Recommendation;
import com.example.service.RecommendationService;
import com.example.repository.RecommendationRepository;
import org.springframework.http.ResponseEntity;
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

    @DeleteMapping("/user/{username}/batch/{batchId}/track/{trackId}")
    public ResponseEntity<String> deleteRecommendationFromBatch(
            @PathVariable String username,
            @PathVariable String batchId,
            @PathVariable Long trackId) {
        try {
            recommendationService.deleteRecommendationFromBatch(username, batchId, trackId);
            return ResponseEntity.ok("Recommendation removed successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/user/{username}/batch/{batchId}")
    public ResponseEntity<Void> deleteRecommendationBatch(
            @PathVariable String username,
            @PathVariable String batchId) {
        try {
            recommendationService.deleteRecommendationBatch(username, batchId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users")
    public List<String> getUsersWithRecommendations() {
        return recommendationRepository.findDistinctUsernames();
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveRecommendations(@RequestParam String username, @RequestBody List<TrackDto> tracks) {
        recommendationService.saveRecommendations(username, tracks);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<Object> getRecommendationsForUser(@PathVariable String username,
            @RequestParam(required = false) String spotifyId, @RequestParam(required = false) String batchId) {
        Object result = recommendationService.getRecommendationsForUser(username, spotifyId, batchId);
        return ResponseEntity.ok(result);
    }
}

