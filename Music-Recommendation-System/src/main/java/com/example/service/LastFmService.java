package com.example.service;

import com.example.dto.TagDto;
import com.example.dto.TrackDto;
import com.example.model.*;
import com.example.app.TagVectorMath;
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
import java.time.ZoneId;
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

    @Transactional
    public List<TrackDto> fetchLovedTracks(String username, String spotifyId) {
        String url = "https://ws.audioscrobbler.com/2.0/" +
                "?method=user.getlovedtracks" +
                "&user=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
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

        User user = userRepository.findByLastfmUsername(username).orElse(null);
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

        List<Track> toPersist = new ArrayList<>();
        Map<Track, Set<String>> trackToTagNames = new HashMap<>();

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

            if (track.getTags() == null || track.getTags().isEmpty()) {
                String artistEnc = URLEncoder.encode(artist, StandardCharsets.UTF_8);
                String titleEnc = URLEncoder.encode(title, StandardCharsets.UTF_8);
                String tagUrl = "https://ws.audioscrobbler.com/2.0/?method=track.gettoptags" +
                        "&artist=" + artistEnc +
                        "&track=" + titleEnc +
                        "&api_key=" + apiKey +
                        "&format=json";
                try {
                    ResponseEntity<Map> tagResponse = restTemplate.getForEntity(tagUrl, Map.class);
                    Map tagBody = tagResponse.getBody();

                    if (tagBody != null && tagBody.containsKey("toptags")) {
                        Map<String, Object> toptags = (Map<String, Object>) tagBody.get("toptags");
                        Object tagObj = toptags.get("tag");

                        Set<String> tagNames = new HashSet<>();
                        if (tagObj instanceof List<?> tagListRaw) {
                            int cnt = 0;
                            for (Object o : tagListRaw) {
                                if (o instanceof Map tagMap) {
                                    String tagName = (String) tagMap.get("name");
                                    if (tagName != null) {
                                        tagNames.add(tagName);
                                        if (++cnt >= 10) break;
                                    }
                                }
                            }
                        }
                        if (!tagNames.isEmpty()) {
                            trackToTagNames.put(track, tagNames);
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            toPersist.add(track);
        }

        Set<String> allTagNames = trackToTagNames.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Tag> tagCache = new HashMap<>();
        List<Tag> newTags = new ArrayList<>();
        for (String name : allTagNames) {
            Tag tag = tagRepository.findByName(name).orElse(null);
            if (tag == null) {
                Tag t = new Tag();
                t.setName(name);
                newTags.add(t);
                tagCache.put(name, t);
            } else {
                tagCache.put(name, tag);
            }
        }
        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags);
        }

        for (Map.Entry<Track, Set<String>> e : trackToTagNames.entrySet()) {
            Track track = e.getKey();
            Set<String> names = e.getValue();
            Set<Tag> tags = names.stream()
                    .map(tagCache::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!tags.isEmpty()) {
                track.setTags(tags);
            }
        }

        if (!toPersist.isEmpty()) {
            trackRepository.saveAll(toPersist);
        }

        statsService.updateIfIncreased();

        return toPersist.stream()
                .map(track -> new TrackDto(
                        track.getId(),
                        track.getTitle(),
                        track.getArtist(),
                        track.getSpotifyId(),
                        track.getLastfmId(),
                        track.getSource(),
                        track.getTags() == null ? Set.of() :
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

        ExecutorService pool = Executors.newFixedThreadPool(Math.min(Math.max(selectedTracks.size(), 1), 8));
        Map<String, Integer> matchCounts = new ConcurrentHashMap<>();

        List<Callable<Void>> tasks = new ArrayList<>();
        for (Track base : selectedTracks) {
            tasks.add(() -> {
                String artistEnc = URLEncoder.encode(base.getArtist(), StandardCharsets.UTF_8);
                String titleEnc = URLEncoder.encode(base.getTitle(), StandardCharsets.UTF_8);
                String url = "https://ws.audioscrobbler.com/2.0/?method=track.getsimilar"
                        + "&artist=" + artistEnc
                        + "&track=" + titleEnc
                        + "&api_key=" + apiKey
                        + "&format=json";

                try {
                    ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                    Map body = response.getBody();
                    if (body == null || !body.containsKey("similartracks")) return null;

                    Map similar = (Map) body.get("similartracks");
                    List<Map<String, Object>> trackList = (List<Map<String, Object>>) similar.get("track");
                    if (trackList == null || trackList.isEmpty()) return null;

                    int lim = Math.min(trackList.size(), 40);
                    for (int i = 0; i < lim; i++) {
                        Map<String, Object> trackMap = trackList.get(i);
                        Map<String, Object> artistMap = (Map<String, Object>) trackMap.get("artist");
                        String simArtist = (String) artistMap.get("name");
                        String simTitle = (String) trackMap.get("name");
                        if (simArtist == null || simTitle == null) continue;
                        String key = simArtist + "::" + simTitle;
                        matchCounts.merge(key, 1, Integer::sum);
                    }
                } catch (Exception ignored) {
                }
                return null;
            });
        }

        try {
            if (!tasks.isEmpty()) {
                List<Future<Void>> futures = pool.invokeAll(tasks);
                for (Future<Void> f : futures) {
                    try { f.get(); } catch (Exception ignored) {}
                }
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdown();
        }

        if (matchCounts.isEmpty()) {
            throw new NoSuchElementException("No recommendations found for selected tracks.");
        }

        final int K = 60;
        List<String> topKKeys = matchCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(K)
                .map(Map.Entry::getKey)
                .toList();

        Map<String, Track> keyToTrack = new LinkedHashMap<>();
        List<Track> newTracks = new ArrayList<>();
        for (String key : topKKeys) {
            String[] parts = key.split("::", 2);
            String simArtist = parts[0];
            String simTitle = parts.length > 1 ? parts[1] : "";
            Track track = trackRepository.findByTitleAndArtist(simTitle, simArtist).orElse(null);
            if (track == null) {
                track = new Track();
                track.setArtist(simArtist);
                track.setTitle(simTitle);
                track.setSource("lastfm");
                newTracks.add(track);
            }
            keyToTrack.put(key, track);
        }
        if (!newTracks.isEmpty()) {
            trackRepository.saveAll(newTracks);
        }

        Map<String, Set<String>> keyToTagNames = new HashMap<>();
        for (Map.Entry<String, Track> e : keyToTrack.entrySet()) {
            Track track = e.getValue();
            if (track.getTags() != null && !track.getTags().isEmpty()) {
                continue;
            }
            String[] parts = e.getKey().split("::", 2);
            String simArtist = parts[0];
            String simTitle = parts.length > 1 ? parts[1] : "";

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
                        Set<String> tagNames = tagListRaw.stream()
                                .filter(Map.class::isInstance)
                                .map(Map.class::cast)
                                .map(m -> (String) m.get("name"))
                                .filter(Objects::nonNull)
                                .limit(10)
                                .collect(Collectors.toCollection(LinkedHashSet::new));
                        if (!tagNames.isEmpty()) {
                            keyToTagNames.put(e.getKey(), tagNames);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        Set<String> allNames = keyToTagNames.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Tag> tagCache = new HashMap<>();
        List<Tag> toCreateTags = new ArrayList<>();
        for (String name : allNames) {
            Tag tag = tagRepository.findByName(name).orElse(null);
            if (tag == null) {
                Tag t = new Tag();
                t.setName(name);
                toCreateTags.add(t);
                tagCache.put(name, t);
            } else {
                tagCache.put(name, tag);
            }
        }
        if (!toCreateTags.isEmpty()) {
            tagRepository.saveAll(toCreateTags);
        }

        List<Track> tracksToUpdate = new ArrayList<>();
        for (Map.Entry<String, Set<String>> e : keyToTagNames.entrySet()) {
            Track t = keyToTrack.get(e.getKey());
            if (t == null) continue;
            Set<Tag> tags = e.getValue().stream()
                    .map(tagCache::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!tags.isEmpty()) {
                t.setTags(tags);
                tracksToUpdate.add(t);
            }
        }
        if (!tracksToUpdate.isEmpty()) {
            trackRepository.saveAll(tracksToUpdate);
        }

        List<Track> idfPool = new ArrayList<>();
        idfPool.addAll(selectedTracks);
        idfPool.addAll(keyToTrack.values());

        Map<String, Double> idf = TagVectorMath.computeIdf(idfPool);

        Map<Track, Map<String, Double>> vecByTrack = new IdentityHashMap<>();
        for (Track t : selectedTracks) {
            vecByTrack.put(t, TagVectorMath.tfidfVectorOfTrack(t, idf));
        }
        for (Track t : keyToTrack.values()) {
            vecByTrack.put(t, TagVectorMath.tfidfVectorOfTrack(t, idf));
        }

        Map<String, Double> userVec = new HashMap<>();
        for (Track t : selectedTracks) {
            Map<String, Double> v = vecByTrack.getOrDefault(t, Map.of());
            if (!v.isEmpty()) userVec = TagVectorMath.addVectors(userVec, v);
        }
        userVec = TagVectorMath.l2Normalize(userVec);

        int maxMatch = matchCounts.values().stream().mapToInt(i -> i).max().orElse(1);

        final double ALPHA = 0.7;
        final double BETA  = 0.3;
        final double THRESHOLD = 0.15;

        class Scored {
            final Track t;
            final double cosine;
            final int match;
            final double score;
            Scored(Track t, double cosine, int match, double score) {
                this.t = t; this.cosine = cosine; this.match = match; this.score = score;
            }
        }

        List<Scored> scored = new ArrayList<>();
        for (var e : keyToTrack.entrySet()) {
            Track t = e.getValue();
            Map<String, Double> v = vecByTrack.getOrDefault(t, Map.of());
            double cos = TagVectorMath.cosineSparse(userVec, v);
            int m = matchCounts.getOrDefault(e.getKey(), 0);
            double mNorm = (maxMatch > 0) ? (m / (double) maxMatch) : 0.0;
            double s = ALPHA * cos + BETA * mNorm;
            scored.add(new Scored(t, cos, m, s));
        }

        List<Track> finalTracks = scored.stream()
                .sorted(Comparator.comparingDouble((Scored s) -> s.score).reversed())
                .filter(s -> s.score >= THRESHOLD)
                .limit(25)
                .map(s -> s.t)
                .toList();

        if (finalTracks.isEmpty()) {
            throw new NoSuchElementException("No recommendations found for selected tracks.");
        }

        List<Track> needSpotify = finalTracks.stream()
                .filter(t -> t.getSpotifyId() == null || t.getSpotifyId().isBlank())
                .toList();

        for (Track track : needSpotify) {
            try {
                String foundSpotifyId = spotifyService.searchSpotifyTrack(
                        spotifyUser.getAccessToken(),
                        track.getTitle(),
                        track.getArtist()
                );
                if (foundSpotifyId != null && !foundSpotifyId.isBlank()) {
                    track.setSpotifyId(foundSpotifyId);
                }
            } catch (Exception ignored) {
            }
        }
        if (!needSpotify.isEmpty()) {
            trackRepository.saveAll(needSpotify);
        }

        String batchId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));

        List<Recommendation> recommendations = finalTracks.stream()
                .filter(t -> !recommendationRepository.existsByUserAndTrack(user, t))
                .map(t -> {
                    Recommendation rec = new Recommendation();
                    rec.setUser(user);
                    rec.setTrack(t);
                    rec.setBatchId(batchId);
                    rec.setCreatedAt(now);
                    return rec;
                })
                .toList();

        if (recommendations.isEmpty()) {
            throw new IllegalStateException("Recommendations for this selection already exist.");
        }

        recommendationRepository.saveAll(recommendations);
        statsService.updateIfIncreased();

        return finalTracks.stream()
                .map(t -> new TrackDto(
                        t.getId(),
                        t.getTitle(),
                        t.getArtist(),
                        t.getSpotifyId(),
                        t.getLastfmId(),
                        t.getSource(),
                        Optional.ofNullable(t.getTags()).orElse(Set.of()).stream()
                                .map(tag -> {
                                    TagDto dto = new TagDto();
                                    dto.setName(tag.getName());
                                    return dto;
                                })
                                .collect(Collectors.toSet())
                ))
                .toList();
    }
}