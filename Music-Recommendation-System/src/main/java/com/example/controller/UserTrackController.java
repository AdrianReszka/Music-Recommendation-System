package com.example.controller;

import com.example.model.UserTrack;
import com.example.service.UserTrackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/user-tracks")
public class UserTrackController {

    private final UserTrackService userTrackService;

    public UserTrackController(UserTrackService userTrackService) {
        this.userTrackService = userTrackService;
    }

    @GetMapping
    public List<UserTrack> getAll() {
        return userTrackService.getAll();
    }

    @PostMapping
    public UserTrack create(@RequestBody UserTrack track) {
        return userTrackService.create(track);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userTrackService.delete(id);
    }
}

