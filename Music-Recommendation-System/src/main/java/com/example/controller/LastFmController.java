package com.example.controller;

import com.example.dto.TrackDto;
import com.example.model.Track;
import com.example.service.LastFmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/lastfm")
public class LastFmController {

    private final LastFmService lastFmService;

    public LastFmController(LastFmService lastFmService) {
        this.lastFmService = lastFmService;
    }

    @GetMapping("/loved")
    public ResponseEntity<List<TrackDto>> getLovedTracks(@RequestParam String username) {
        List<TrackDto> lovedTracks = lastFmService.fetchLovedTracks(username);
        return ResponseEntity.ok(lovedTracks);
    }

}