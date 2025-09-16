package com.example.repository;

import com.example.model.Recommendation;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Query("SELECT DISTINCT r.user.lastfmUsername FROM Recommendation r")
    List<String> findDistinctUsernames();

    List<Recommendation> findByUser(User user);

}