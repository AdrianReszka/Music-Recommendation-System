package com.example.repository;

import com.example.model.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

}