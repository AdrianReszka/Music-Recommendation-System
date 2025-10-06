package com.example.service;

import com.example.model.SpotifyUserLink;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.repository.SpotifyUserLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SpotifyUserLinkRepository spotifyUserLinkRepository;

    public List<User> getUsersLinkedToSpotify(String spotifyId) {
        List<SpotifyUserLink> links = spotifyUserLinkRepository.findBySpotifyUser_SpotifyId(spotifyId);
        return links.stream()
                .map(SpotifyUserLink::getUser)
                .collect(Collectors.toList());
    }

    public List<User> getAll() {
        return userRepository.findAll();
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

