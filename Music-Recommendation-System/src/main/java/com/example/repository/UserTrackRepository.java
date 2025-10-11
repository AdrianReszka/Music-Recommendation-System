package com.example.repository;

import com.example.model.Track;
import com.example.model.User;
import com.example.model.UserTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserTrackRepository extends JpaRepository<UserTrack, Long> {

    boolean existsByUserAndTrack(User user, Track track);

    boolean existsByUser(User user);

    List<UserTrack> findByUser(User user);

}