package com.example.controller;

import com.example.model.Recommendation;
import com.example.repository.RecommendationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/recommendations")
public class RecommendationController {

    private final RecommendationRepository recommendationRepository;

    public RecommendationController(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @GetMapping
    public List<Recommendation> getAll() {
        return recommendationRepository.findAll();
    }

    @GetMapping("/{id}")
    public Recommendation getById(@PathVariable Long id) {
        return recommendationRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Recommendation create(@RequestBody Recommendation entity) {
        return recommendationRepository.save(entity);
    }

    @PutMapping("/{id}")
    public Recommendation update(@PathVariable Long id, @RequestBody Recommendation updated) {
        updated.setId(id);
        return recommendationRepository.save(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        recommendationRepository.deleteById(id);
    }
}
