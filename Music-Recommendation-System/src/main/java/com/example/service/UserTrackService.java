package com.example.service;

import com.example.dto.TagDto;
import com.example.dto.TrackDto;
import com.example.model.Tag;
import com.example.model.Track;
import com.example.model.User;
import com.example.model.UserTrack;
import com.example.repository.TrackRepository;
import com.example.repository.UserRepository;
import com.example.repository.UserTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserTrackService {

    private final UserRepository userRepository;
    private final UserTrackRepository userTrackRepository;
    private final TrackRepository trackRepository;
    private final LastFmService lastFmService;

    public UserTrackService(UserRepository userRepository,
                            UserTrackRepository userTrackRepository,
                            LastFmService lastFmService, TrackRepository trackRepository) {
        this.userRepository = userRepository;
        this.userTrackRepository = userTrackRepository;
        this.lastFmService = lastFmService;
        this.trackRepository = trackRepository;
    }

    public List<UserTrack> getAll() {
        return userTrackRepository.findAll();
    }

    public UserTrack create(UserTrack track) {
        return userTrackRepository.save(track);
    }

    public void delete(Long id) {
        userTrackRepository.deleteById(id);
    }

    public List<TrackDto> importLovedTracksFromLastFm(String username, String spotifyId) {
        List<TrackDto> lovedTrackDtos = lastFmService.fetchLovedTracks(username,  spotifyId);

        if (lovedTrackDtos.isEmpty()) {
            return lovedTrackDtos;
        }

        User user = userRepository.findByLastfmUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setLastfmUsername(username);
                    return userRepository.save(newUser);
                });

        for (TrackDto dto : lovedTrackDtos) {
            Track track = trackRepository.findByTitleAndArtist(dto.getTitle(), dto.getArtist())
                    .orElseThrow(() -> new RuntimeException("Track not found after fetch: " + dto.getTitle()));

            boolean exists = userTrackRepository.existsByUserAndTrack(user, track);
            if (!exists) {
                UserTrack userTrack = new UserTrack();
                userTrack.setUser(user);
                userTrack.setTrack(track);
                userTrackRepository.save(userTrack);
            }
        }

        return lovedTrackDtos;
    }

    public List<TrackDto> getTracksForUser(String username) {
        User user = userRepository.findByLastfmUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));

        List<UserTrack> userTracks = userTrackRepository.findByUser(user);

        return userTracks.stream()
                .map(ut -> {
                    Track t = ut.getTrack();
                    return new TrackDto(
                            t.getId(),
                            t.getTitle(),
                            t.getArtist(),
                            t.getSpotifyId(),
                            t.getLastfmId(),
                            t.getSource(),
                            t.getTags().stream()
                                    .map(tag -> {
                                        TagDto dto = new TagDto();
                                        dto.setName(tag.getName());
                                        return dto;
                                    })
                                    .collect(Collectors.toSet())
                    );
                })
                .toList();
    }
}

