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

        if (body == null || !body.containsKey("lovedtracks")) return List.of();

        Map lovedTracks = (Map) body.get("lovedtracks");
        List<Map<String, Object>> trackList = (List<Map<String, Object>>) lovedTracks.get("track");

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
        User user = userRepository.findByLastfmUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<Track> baseTracks = trackRepository.findAllWithTagsByIdIn(selectedTrackIds);

        Set<String> alreadyProcessed = new HashSet<>();
        List<TrackDto> recommendations = new ArrayList<>();

        for (Track base : baseTracks) {
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
                    if (alreadyProcessed.contains(key)) continue;
                    alreadyProcessed.add(key);

                    Set<Tag> tags = new HashSet<>();
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
                                        Tag tag = tagRepository.findByName(tagName)
                                                .orElseGet(() -> {
                                                    Tag newTag = new Tag();
                                                    newTag.setName(tagName);
                                                    return tagRepository.saveAndFlush(newTag);
                                                });
                                        tags.add(tag);
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
                                t.setTags(tags);
                                return trackRepository.save(t);
                            });

                    Recommendation rec = new Recommendation();
                    rec.setUser(user);
                    rec.setTrack(track);
                    recommendationRepository.save(rec);

                    recommendations.add(new TrackDto(
                            track.getId(),
                            track.getTitle(),
                            track.getArtist(),
                            track.getSpotifyId(),
                            track.getLastfmId(),
                            track.getSource(),
                            tags.stream().map(tag -> {
                                TagDto dto = new TagDto();
                                dto.setName(tag.getName());
                                return dto;
                            }).collect(Collectors.toSet())
                    ));
                }

                System.out.println("Last.fm recommendations for user: " + username);
                for (TrackDto track : recommendations) {
                    String tagList = track.getTags().stream()
                            .map(TagDto::getName)
                            .collect(Collectors.joining(", "));

                    System.out.println("→ " + track.getArtist() + " - " + track.getTitle() + " [" + tagList + "]");
                }

            } catch (Exception e) {
                System.err.println("Error while fetching similar for: " + base.getTitle() + " - " + e.getMessage());
            }
        }

        return recommendations;
    }

}
