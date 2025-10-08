package com.example.repository;

import com.example.model.Recommendation;
import com.example.model.Track;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Query("SELECT DISTINCT r.user.lastfmUsername FROM Recommendation r")
    List<String> findDistinctUsernames();

    @Query("SELECT DISTINCT r.batchId, r.createdAt FROM Recommendation r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Object[]> findDistinctBatchIdsByUser(@Param("userId") Long userId);

    List<Recommendation> findByUserAndBatchId(User user, String batchId);

    boolean existsByUserAndTrack(User user, Track track);

}