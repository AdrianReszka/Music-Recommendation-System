package com.example.controller;

import com.example.dto.TrackDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/musicapp/lastfm")
public class LastFmController {

    private final String apiKey = "dc593bcc5c9f45cab8ddd59c71ea9029";

    @GetMapping("/loved")
    public List<TrackDto> getLovedTracks() {
        String username = "Adicom02";
        String url = "https://ws.audioscrobbler.com/2.0/?method=user.getlovedtracks&user=" +
                username + "&api_key=" + apiKey + "&limit=10&format=json";

        RestTemplate restTemplate = new RestTemplate();
        var response = restTemplate.getForEntity(url, Map.class);

        List<Map<String, Object>> rawTracks =
                (List<Map<String, Object>>) ((Map<String, Object>) Objects.requireNonNull(response.getBody())
                        .get("lovedtracks")).get("track");

        return rawTracks.stream().map(track -> {
            String title = (String) track.get("name");
            String artist = (String) ((Map<String, Object>) track.get("artist")).get("name");
            String lastfmId = (String) track.get("mbid");
            return new TrackDto(title, artist, null, lastfmId, "lastfm");
        }).toList();
    }

}
