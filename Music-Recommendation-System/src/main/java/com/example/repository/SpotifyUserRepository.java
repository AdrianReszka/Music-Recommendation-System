package com.example.repository;

import com.example.model.SpotifyUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpotifyUserRepository extends JpaRepository<SpotifyUser, Integer> {

    Optional<SpotifyUser> findByUsername(String username);

    Optional<SpotifyUser> findBySpotifyId(String spotifyId);
}
