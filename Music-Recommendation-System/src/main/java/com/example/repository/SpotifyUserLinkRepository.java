package com.example.repository;

import com.example.model.SpotifyUser;
import com.example.model.User;
import com.example.model.SpotifyUserLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SpotifyUserLinkRepository extends JpaRepository<SpotifyUserLink, Long> {

    boolean existsBySpotifyUserAndUser(SpotifyUser spotifyUser, User user);

    List<SpotifyUserLink> findBySpotifyUser_SpotifyId(String spotifyId);

}
