package com.example.service;

import com.example.dto.TagDto;
import com.example.dto.TrackDto;
import com.example.model.*;
import com.example.repository.TrackRepository;
import com.example.repository.UserRepository;
import com.example.repository.UserTrackRepository;
import com.example.repository.SpotifyUserLinkRepository;
import com.example.repository.SpotifyUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserTrackService {

    private final UserRepository userRepository;
    private final UserTrackRepository userTrackRepository;
    private final TrackRepository trackRepository;
    private final LastFmService lastFmService;
    private final SpotifyUserRepository spotifyUserRepository;
    private final SpotifyUserLinkRepository spotifyUserLinkRepository;

    public UserTrackService(UserRepository userRepository,
                            UserTrackRepository userTrackRepository,
                            LastFmService lastFmService, TrackRepository trackRepository,  SpotifyUserLinkRepository spotifyUserLinkRepository,
                            SpotifyUserRepository spotifyUserRepository) {
        this.userRepository = userRepository;
        this.userTrackRepository = userTrackRepository;
        this.lastFmService = lastFmService;
        this.trackRepository = trackRepository;
        this.spotifyUserRepository = spotifyUserRepository;
        this.spotifyUserLinkRepository = spotifyUserLinkRepository;
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
                .orElse(null);

        if (user == null) {
            user = new User();
            user.setLastfmUsername(username);
            userRepository.save(user);
        }

        if (spotifyId != null) {
            SpotifyUser spotifyUser = spotifyUserRepository.findBySpotifyId(spotifyId)
                    .orElseThrow(() -> new RuntimeException("Spotify user not found: " + spotifyId));

            boolean exists = spotifyUserLinkRepository.existsBySpotifyUserAndUser(spotifyUser, user);
            if (!exists) {
                SpotifyUserLink link = new SpotifyUserLink();
                link.setSpotifyUser(spotifyUser);
                link.setUser(user);
                spotifyUserLinkRepository.save(link);
            }
        }

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

    public List<TrackDto> getTracksForUser(String username, String spotifyId) {
        User user = userRepository.findByLastfmUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));

        if (spotifyId != null) {
            SpotifyUser spotifyUser = spotifyUserRepository.findBySpotifyId(spotifyId)
                    .orElseThrow(() -> new NoSuchElementException("Spotify user not found: " + spotifyId));

            boolean hasAccess = spotifyUserLinkRepository.existsBySpotifyUserAndUser(spotifyUser, user);
            if (!hasAccess) {
                throw new SecurityException("Access denied: Spotify user not linked with this Last.fm account");
            }
        }

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

