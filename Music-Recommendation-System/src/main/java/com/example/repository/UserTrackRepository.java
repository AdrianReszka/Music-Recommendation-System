package com.example.repository;

import com.example.model.Track;
import com.example.model.User;
import com.example.model.UserTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTrackRepository extends JpaRepository<UserTrack, Long> {

    boolean existsByUserAndTrack(User user, Track track);

    List<UserTrack> findByUser(User user);
}