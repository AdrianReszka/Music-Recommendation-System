package com.example.service;

import com.example.model.SpotifyUser;
import com.example.model.SpotifyUserLink;
import com.example.model.User;
import com.example.repository.SpotifyUserRepository;
import com.example.repository.UserRepository;
import com.example.repository.SpotifyUserLinkRepository;
import com.example.repository.UserTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserTrackRepository userTrackRepository;
    private final SpotifyUserRepository spotifyUserRepository;
    private final SpotifyUserLinkRepository spotifyUserLinkRepository;

    public List<Map<String, String>> getUsersLinkedToSpotify(String spotifyId) {
        List<SpotifyUserLink> links = spotifyUserLinkRepository.findBySpotifyUser_SpotifyId(spotifyId);

        return links.stream()
                .map(SpotifyUserLink::getUser)
                .filter(userTrackRepository::existsByUser)
                .map(user -> Map.of("lastfmUsername", user.getLastfmUsername()))
                .filter(dto -> dto.get("lastfmUsername") != null && !dto.get("lastfmUsername").isEmpty())
                .toList();
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void unlinkSpotifyAccount(String spotifyId, String lastfmUsername) {
        SpotifyUser spotifyUser = spotifyUserRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new RuntimeException("Spotify user not found"));

        User lastfmUser = userRepository.findByLastfmUsername(lastfmUsername)
                .orElseThrow(() -> new RuntimeException("Last.fm user not found"));

        Optional<SpotifyUserLink> link = spotifyUserLinkRepository.findBySpotifyUserAndUser(spotifyUser, lastfmUser);

        if (link.isEmpty()) {
            throw new RuntimeException("Link between Spotify and Last.fm accounts not found");
        }

        spotifyUserLinkRepository.delete(link.get());
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}

