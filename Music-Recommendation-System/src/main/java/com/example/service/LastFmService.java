package com.example.service;

import com.example.dto.TagDto;
import com.example.dto.TrackDto;
import com.example.model.*;
import com.example.repository.RecommendationRepository;
import com.example.repository.TagRepository;
import com.example.repository.TrackRepository;
import com.example.repository.UserRepository;
import com.example.repository.SpotifyUserRepository;
import com.example.repository.SpotifyUserLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LastFmService {

    @Value("${lastfm.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final StatsService statsService;
    private final TrackRepository trackRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final RecommendationRepository recommendationRepository;
    private final SpotifyUserRepository spotifyUserRepository;
    private final SpotifyUserLinkRepository spotifyUserLinkRepository;

    @Transactional
    public List<TrackDto> fetchLovedTracks(String username, String spotifyId) {
        String url = "https://ws.audioscrobbler.com/2.0/" +
                "?method=user.getlovedtracks" +
                "&user=" + username +
                "&api_key=" + apiKey +
                "&format=json";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map body = response.getBody();

        if (body == null || !body.containsKey("lovedtracks")) {
            throw new NoSuchElementException("Last.fm user not found: " + username);
        }

        Map lovedTracks = (Map) body.get("lovedtracks");
        List<Map<String, Object>> trackList = (List<Map<String, Object>>) lovedTracks.get("track");

        if (trackList == null || trackList.isEmpty()) {
            return Collections.emptyList();
        }

        User user = userRepository.findByLastfmUsername(username)
                .orElse(null);

        if (user == null) {
            user = new User();
            user.setLastfmUsername(username);
            userRepository.save(user);
        }

        if (spotifyId != null) {
            User finalUser = user;
            spotifyUserRepository.findBySpotifyId(spotifyId).ifPresent(spotifyUser -> {
                boolean exists = spotifyUserLinkRepository.existsBySpotifyUserAndUser(spotifyUser, finalUser);
                if (!exists) {
                    SpotifyUserLink link = new SpotifyUserLink();
                    link.setSpotifyUser(spotifyUser);
                    link.setUser(finalUser);
                    spotifyUserLinkRepository.save(link);

                    statsService.updateIfIncreased();
                }
            });
        }

        List<Track> savedTracks = new ArrayList<>();

        for (Map<String, Object> trackMap : trackList) {
            String title = (String) trackMap.get("name");
            String mbid = (String) trackMap.get("mbid");

            Map<String, Object> artistMap = (Map<String, Object>) trackMap.get("artist");
            String artist = (String) artistMap.get("name");

            Optional<Track> existing = trackRepository.findByTitleAndArtist(title, artist);
            Track track = existing.orElseGet(Track::new);

            track.setTitle(title);
            track.setArtist(artist);
            track.setLastfmId(mbid);
            track.setSource("lastfm");

            String tagUrl = "https://ws.audioscrobbler.com/2.0/?method=track.gettoptags" +
                    "&artist=" + artist.replace(" ", "%20") +
                    "&track=" + title.replace(" ", "%20") +
                    "&api_key=" + apiKey +
                    "&format=json";

            try {
                ResponseEntity<Map> tagResponse = restTemplate.getForEntity(tagUrl, Map.class);
                Map tagBody = tagResponse.getBody();

                if (tagBody != null && tagBody.containsKey("toptags")) {
                    Map<String, Object> toptags = (Map<String, Object>) tagBody.get("toptags");
                    Object tagObj = toptags.get("tag");

                    if (tagObj instanceof List<?> tagListRaw) {
                        Set<Tag> tags = new HashSet<>();

                        for (Object o : tagListRaw) {
                            if (o instanceof Map tagMap) {
                                String tagName = (String) tagMap.get("name");

                                Tag tag = tagRepository.findByName(tagName)
                                        .orElseGet(() -> {
                                            Tag newTag = new Tag();
                                            newTag.setName(tagName);
                                            return tagRepository.saveAndFlush(newTag);
                                        });

                                tags.add(tag);
                            }
                        }

                        track.setTags(tags);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch tags for track: " + title + " | " + e.getMessage());
            }

            trackRepository.save(track);
            savedTracks.add(track);
        }

        statsService.updateIfIncreased();

        return savedTracks.stream()
                .map(track -> new TrackDto(
                        track.getId(),
                        track.getTitle(),
                        track.getArtist(),
                        track.getSpotifyId(),
                        track.getLastfmId(),
                        track.getSource(),
                        track.getTags().stream()
                                .map(tag -> {
                                    TagDto dto = new TagDto();
                                    dto.setName(tag.getName());
                                    return dto;
                                })
                                .collect(Collectors.toSet())
                ))
                .toList();
    }

    @Transactional
    public List<TrackDto> fetchSimilarTracksForUser(String username, String spotifyId, List<Long> selectedTrackIds) {
        var spotifyUser = spotifyUserRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new SecurityException("Spotify user not found or not logged in."));

        User user = userRepository.findByLastfmUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));

        if (!spotifyUserLinkRepository.existsBySpotifyUserAndUser(spotifyUser, user)) {
            throw new SecurityException("This Spotify account is not linked with Last.fm user: " + username);
        }

        List<Track> selectedTracks = trackRepository.findAllWithTagsByIdIn(selectedTrackIds);

        Set<String> userPreferredTags = selectedTracks.stream()
                .flatMap(t -> t.getTags().stream())
                .map(Tag::getName)
                .collect(Collectors.toSet());

        record ScoredTrack(Track track, int matchCount, int sharedTagCount) {
            int score() { return matchCount * 3 + sharedTagCount; }
        }

        Map<String, ScoredTrack> scoredMap = new HashMap<>();

        for (Track base : selectedTracks) {
            String artist = URLEncoder.encode(base.getArtist(), StandardCharsets.UTF_8);
            String title = URLEncoder.encode(base.getTitle(), StandardCharsets.UTF_8);

            String url = "https://ws.audioscrobbler.com/2.0/?method=track.getsimilar"
                    + "&artist=" + artist
                    + "&track=" + title
                    + "&api_key=" + apiKey
                    + "&format=json";

            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                Map body = response.getBody();
                if (body == null || !body.containsKey("similartracks")) continue;

                Map similar = (Map) body.get("similartracks");
                List<Map<String, Object>> trackList = (List<Map<String, Object>>) similar.get("track");
                if (trackList == null) continue;

                if (trackList.size() > 40)
                    trackList = trackList.subList(0, 40);

                for (Map<String, Object> trackMap : trackList) {
                    Map<String, Object> artistMap = (Map<String, Object>) trackMap.get("artist");
                    String simArtist = (String) artistMap.get("name");
                    String simTitle = (String) trackMap.get("name");
                    String key = simArtist + "::" + simTitle;

                    Track track = trackRepository.findByTitleAndArtist(simTitle, simArtist)
                            .orElseGet(() -> {
                                Track t = new Track();
                                t.setTitle(simTitle);
                                t.setArtist(simArtist);
                                t.setSource("lastfm");
                                return trackRepository.save(t);
                            });

                    if (track.getTags() == null || track.getTags().isEmpty()) {
                        try {
                            String tagUrl = "https://ws.audioscrobbler.com/2.0/?method=track.gettoptags" +
                                    "&artist=" + URLEncoder.encode(simArtist, StandardCharsets.UTF_8) +
                                    "&track=" + URLEncoder.encode(simTitle, StandardCharsets.UTF_8) +
                                    "&api_key=" + apiKey +
                                    "&format=json";

                            ResponseEntity<Map> tagResponse = restTemplate.getForEntity(tagUrl, Map.class);
                            Map tagBody = tagResponse.getBody();

                            if (tagBody != null && tagBody.containsKey("toptags")) {
                                Map<String, Object> toptags = (Map<String, Object>) tagBody.get("toptags");
                                Object tagObj = toptags.get("tag");

                                if (tagObj instanceof List<?> tagListRaw) {
                                    Set<Tag> tags = tagListRaw.stream()
                                            .filter(Map.class::isInstance)
                                            .map(Map.class::cast)
                                            .map(m -> (String) m.get("name"))
                                            .filter(Objects::nonNull)
                                            .limit(10)
                                            .map(tagName -> tagRepository.findByName(tagName)
                                                    .orElseGet(() -> {
                                                        Tag newTag = new Tag();
                                                        newTag.setName(tagName);
                                                        return tagRepository.save(newTag);
                                                    }))
                                            .collect(Collectors.toSet());

                                    track.setTags(tags);
                                    track = trackRepository.save(track);
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    int shared = (int) track.getTags().stream()
                            .map(Tag::getName)
                            .filter(userPreferredTags::contains)
                            .count();

                    Track finalTrack = track;
                    scoredMap.merge(
                            key,
                            new ScoredTrack(track, 1, shared),
                            (oldVal, newVal) -> new ScoredTrack(
                                    finalTrack,
                                    oldVal.matchCount() + 1,
                                    oldVal.sharedTagCount() + shared
                            )
                    );
                }
            } catch (Exception e) {
                System.err.println("Error fetching similar for " + base.getTitle() + ": " + e.getMessage());
            }
        }

        List<ScoredTrack> topScored = scoredMap.values().stream()
                .sorted(Comparator.comparingInt(ScoredTrack::score).reversed())
                .limit(25)
                .filter(scored -> scored.score() >= 6)
                .toList();

        String batchId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        List<Recommendation> recommendations = topScored.stream()
                .filter(s -> !recommendationRepository.existsByUserAndTrack(user, s.track()))
                .map(s -> {
                    Recommendation rec = new Recommendation();
                    rec.setUser(user);
                    rec.setTrack(s.track());
                    rec.setBatchId(batchId);
                    rec.setCreatedAt(now);
                    return rec;
                })
                .toList();

        if (!recommendations.isEmpty())
            recommendationRepository.saveAll(recommendations);

        statsService.updateIfIncreased();

        return topScored.stream()
                .map(scored -> {
                    Track t = scored.track();
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