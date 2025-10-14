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
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LastFmService {

    @Value("${lastfm.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final StatsService statsService;
    private final SpotifyService spotifyService;
    private final TrackRepository trackRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final RecommendationRepository recommendationRepository;
    private final SpotifyUserRepository spotifyUserRepository;
    private final SpotifyUserLinkRepository spotifyUserLinkRepository;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private record ScoredTrack(Track track, int matchCount, int sharedTagCount) {
        int score() { return matchCount * 3 + sharedTagCount; }
    }

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

        List<Track> tracksToProcess = new ArrayList<>();

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

            trackRepository.save(track);
            tracksToProcess.add(track);
        }

        List<CompletableFuture<Void>> tagFutures = tracksToProcess.stream()
                .map(track -> CompletableFuture.runAsync(() ->
                        fetchAndSaveTagsForTrack(track), executorService))
                .toList();

        try {
            CompletableFuture.allOf(tagFutures.toArray(new CompletableFuture[0]))
                    .get(120, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.err.println("Timeout while fetching tags for loved tracks");
            tagFutures.forEach(f -> f.cancel(true));
        } catch (Exception e) {
            System.err.println("Error waiting for tag futures: " + e.getMessage());
        }

        statsService.updateIfIncreased();

        return tracksToProcess.stream()
                .map(track -> new TrackDto(
                        track.getId(),
                        track.getTitle(),
                        track.getArtist(),
                        track.getSpotifyId(),
                        track.getLastfmId(),
                        track.getSource(),
                        track.getTags() != null ? track.getTags().stream()
                                .map(tag -> {
                                    TagDto dto = new TagDto();
                                    dto.setName(tag.getName());
                                    return dto;
                                })
                                .collect(Collectors.toSet()) : new HashSet<>()
                ))
                .toList();
    }

    public void fetchAndSaveTagsForTrack(Track track) {
        String tagUrl = "https://ws.audioscrobbler.com/2.0/?method=track.gettags" +
                "&artist=" + URLEncoder.encode(track.getArtist(), StandardCharsets.UTF_8) +
                "&track=" + URLEncoder.encode(track.getTitle(), StandardCharsets.UTF_8) +
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
                    trackRepository.save(track);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch tags for track: " + track.getTitle() + " | " + e.getMessage());
        }
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

        ConcurrentHashMap<String, ScoredTrack> scoredMap = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = selectedTracks.stream()
                .map(base -> CompletableFuture.runAsync(() ->
                        processSimilarTracks(base, scoredMap, userPreferredTags), executorService))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(120, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.err.println("Timeout while fetching similar tracks");
            futures.forEach(f -> f.cancel(true));
        } catch (Exception e) {
            System.err.println("Error waiting for futures: " + e.getMessage());
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

        String accessToken = spotifyUser.getAccessToken();

        topScored.stream()
                .map(ScoredTrack::track)
                .filter(t -> t.getSpotifyId() == null || t.getSpotifyId().isBlank())
                .forEach(track -> {
                    try {
                        var id = spotifyService.searchSpotifyTrack(accessToken, track.getTitle(), track.getArtist());
                        if (id != null) {
                            track.setSpotifyId(id);
                            trackRepository.save(track);
                        }
                    } catch (Exception e) {
                        System.err.println("Could not fetch Spotify ID for " + track.getTitle() + ": " + e.getMessage());
                    }
                });

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
                            t.getTags() != null ? t.getTags().stream()
                                    .map(tag -> {
                                        TagDto dto = new TagDto();
                                        dto.setName(tag.getName());
                                        return dto;
                                    })
                                    .collect(Collectors.toSet()) : new HashSet<>()
                    );
                })
                .toList();
    }

    public void processSimilarTracks(Track base, ConcurrentHashMap<String, ScoredTrack> scoredMap, Set<String> userPreferredTags) {
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
            if (body == null || !body.containsKey("similartracks")) return;

            Map similar = (Map) body.get("similartracks");
            List<Map<String, Object>> trackList = (List<Map<String, Object>>) similar.get("track");
            if (trackList == null) return;

            if (trackList.size() > 40)
                trackList = trackList.subList(0, 40);

            List<Track> tracks = processTrackBatch(trackList);

            for (Track track : tracks) {
                String key = track.getArtist() + "::" + track.getTitle();

                if (track.getTags() == null) {
                    track.setTags(new HashSet<>());
                }

                int shared = (int) track.getTags().stream()
                        .map(Tag::getName)
                        .filter(userPreferredTags::contains)
                        .count();

                scoredMap.merge(
                        key,
                        new ScoredTrack(track, 1, shared),
                        (oldVal, newVal) -> new ScoredTrack(
                                track,
                                oldVal.matchCount() + 1,
                                oldVal.sharedTagCount() + shared
                        )
                );
            }
        } catch (Exception e) {
            System.err.println("Error fetching similar for " + base.getTitle() + ": " + e.getMessage());
        }
    }

    @Transactional
    public List<Track> processTrackBatch(List<Map<String, Object>> trackList) {
        List<Track> result = new ArrayList<>();

        List<Track> existingTracks = trackRepository.findAll();
        Map<String, Track> existingMap = existingTracks.stream()
                .collect(Collectors.toMap(
                        t -> t.getArtist() + ":::" + t.getTitle(),
                        t -> t,
                        (a, b) -> a
                ));

        List<Track> newTracks = new ArrayList<>();

        for (Map<String, Object> trackMap2 : trackList) {
            Map<String, Object> artistMap = (Map<String, Object>) trackMap2.get("artist");
            String simArtist = (String) artistMap.get("name");
            String simTitle = (String) trackMap2.get("name");
            String key = simArtist + ":::" + simTitle;

            Track track = existingMap.get(key);

            if (track == null) {
                track = new Track();
                track.setTitle(simTitle);
                track.setArtist(simArtist);
                track.setSource("lastfm");
                track.setTags(new HashSet<>());
                newTracks.add(track);
                existingMap.put(key, track);
            }

            result.add(track);
        }

        if (!newTracks.isEmpty()) {
            trackRepository.saveAll(newTracks);
            trackRepository.flush();
        }
        return result;
    }
}