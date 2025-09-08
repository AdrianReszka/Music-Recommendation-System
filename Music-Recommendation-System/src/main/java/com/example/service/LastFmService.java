package com.example.service;

import com.example.dto.TagDto;
import com.example.dto.TrackDto;
import com.example.model.Tag;
import com.example.model.Track;
import com.example.repository.TagRepository;
import com.example.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}
