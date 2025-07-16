package com.example.repository;

import com.example.model.UserTrack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTrackRepository extends JpaRepository<UserTrack, Long> {

}