package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Object getAllUsers(@RequestParam(required = false) String spotifyId) {
        if (spotifyId != null && !spotifyId.isEmpty()) {
            return userService.getUsersLinkedToSpotify(spotifyId);
        }
        return userService.getAll();
    }

    @DeleteMapping("/unlink")
    public ResponseEntity<String> unlinkAccount(
            @RequestParam String spotifyId,
            @RequestParam String lastfmUsername) {
        try {
            userService.unlinkSpotifyAccount(spotifyId, lastfmUsername);
            return ResponseEntity.ok("Link successfully removed");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.create(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}

