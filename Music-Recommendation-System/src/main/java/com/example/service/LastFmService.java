package com.example.service;

import com.example.model.Track;
import com.example.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LastFmService {

    @Value("${lastfm.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final TrackRepository trackRepository;

    public List<Track> fetchLovedTracks(String username) {
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

            trackRepository.save(track);
            savedTracks.add(track);
        }

        return savedTracks;
    }
}



