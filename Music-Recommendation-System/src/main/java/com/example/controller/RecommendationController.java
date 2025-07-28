package com.example.controller;

import com.example.model.Recommendation;
import com.example.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
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
}
