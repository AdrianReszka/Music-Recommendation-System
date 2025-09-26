package com.example.service;

import com.example.dto.TagDto;
import com.example.dto.TrackDto;
import com.example.model.Recommendation;
import com.example.model.Tag;
import com.example.model.Track;
import com.example.model.User;
import com.example.repository.RecommendationRepository;
import com.example.repository.TagRepository;
import com.example.repository.TrackRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LastFmService {

    @Value("${lastfm.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final TrackRepository trackRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final RecommendationRepository recommendationRepository;

    public List<TrackDto> fetchLovedTracks(String username) {
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

    public List<TrackDto> fetchSimilarTracksForUser(String username, List<Long> selectedTrackIds) {
        List<Track> selectedTracks = trackRepository.findAllWithTagsByIdIn(selectedTrackIds);

        Set<String> userPreferredTags = selectedTracks.stream()
                .flatMap(t -> t.getTags().stream())
                .map(Tag::getName)
                .collect(Collectors.toSet());

        record ScoredTrack(Track track, int matchCount, int sharedTagCount) {
            int score() {
                return matchCount * 3 + sharedTagCount;
            }

            ScoredTrack increment(Track newTrack, Set<String> newTags, Set<String> preferredTags) {
                int shared = (int) newTags.stream().filter(preferredTags::contains).count();
                return new ScoredTrack(newTrack, matchCount + 1, sharedTagCount + shared);
            }
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

                for (Map<String, Object> trackMap : trackList) {
                    Map<String, Object> artistMap = (Map<String, Object>) trackMap.get("artist");
                    String simArtist = (String) artistMap.get("name");
                    String simTitle = (String) trackMap.get("name");

                    String key = simArtist + "::" + simTitle;

                    Set<String> fetchedTagNames = new HashSet<>();
                    Set<Tag> tagEntities = new HashSet<>();

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
                                for (Object o : tagListRaw) {
                                    if (o instanceof Map tagMap) {
                                        String tagName = (String) tagMap.get("name");
                                        fetchedTagNames.add(tagName);

                                        Tag tag = tagRepository.findByName(tagName)
                                                .orElseGet(() -> {
                                                    Tag newTag = new Tag();
                                                    newTag.setName(tagName);
                                                    return tagRepository.saveAndFlush(newTag);
                                                });

                                        tagEntities.add(tag);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to fetch tags for: " + simTitle);
                    }

                    Track track = trackRepository.findByTitleAndArtist(simTitle, simArtist)
                            .orElseGet(() -> {
                                Track t = new Track();
                                t.setTitle(simTitle);
                                t.setArtist(simArtist);
                                t.setSource("lastfm");
                                t.setTags(tagEntities);
                                return trackRepository.save(t);
                            });

                    if (scoredMap.containsKey(key)) {
                        ScoredTrack updated = scoredMap.get(key).increment(track, fetchedTagNames, userPreferredTags);
                        scoredMap.put(key, updated);
                    } else {
                        int shared = (int) fetchedTagNames.stream().filter(userPreferredTags::contains).count();
                        scoredMap.put(key, new ScoredTrack(track, 1, shared));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error while fetching similar for: " + base.getTitle() + " - " + e.getMessage());
            }
        }

        List<ScoredTrack> topScored = scoredMap.values().stream()
                .sorted(Comparator.comparingInt(ScoredTrack::score).reversed())
                .limit(25)
                .toList();

        List<ScoredTrack> filtered = topScored.stream()
                .filter(scored -> scored.score() >= 6)
                .toList();

        User user = userRepository.findByLastfmUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        for (ScoredTrack scored : filtered) {
            Track track = scored.track;

            boolean exists = recommendationRepository.existsByUserAndTrack(user, track);
            if (!exists) {
                Recommendation rec = new Recommendation();
                rec.setUser(user);
                rec.setTrack(track);
                recommendationRepository.save(rec);
            }
        }

        return filtered.stream()
                .map(scored -> {
                    Track t = scored.track;
                    return new TrackDto(
                            t.getId(),
                            t.getTitle(),
                            t.getArtist(),
                            t.getSpotifyId(),
                            t.getLastfmId(),
                            t.getSource(),
                            t.getTags().stream().map(tag -> {
                                TagDto dto = new TagDto();
                                dto.setName(tag.getName());
                                return dto;
                            }).collect(Collectors.toSet())
                    );
                })
                .toList();
    }
}
