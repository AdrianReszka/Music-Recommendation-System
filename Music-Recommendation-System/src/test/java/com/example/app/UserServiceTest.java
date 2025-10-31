package com.example.app;

import com.example.model.SpotifyUser;
import com.example.model.SpotifyUserLink;
import com.example.model.User;
import com.example.repository.*;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserTrackRepository userTrackRepository;
    @Mock private SpotifyUserRepository spotifyUserRepository;
    @Mock private SpotifyUserLinkRepository spotifyUserLinkRepository;

    @InjectMocks
    private UserService service;

    private User user1;
    private User user2;
    private SpotifyUser spotifyUser;
    private SpotifyUserLink link1;
    private SpotifyUserLink link2;

    @BeforeEach
    void setup() {
        user1 = new User();
        user1.setId(1L);
        user1.setLastfmUsername("test_user");

        user2 = new User();
        user2.setId(2L);
        user2.setLastfmUsername("");

        spotifyUser = new SpotifyUser();
        spotifyUser.setSpotifyId("sp123");

        link1 = new SpotifyUserLink();
        link1.setSpotifyUser(spotifyUser);
        link1.setUser(user1);

        link2 = new SpotifyUserLink();
        link2.setSpotifyUser(spotifyUser);
        link2.setUser(user2);
    }

    @Test
    void getUsersLinkedToSpotify_shouldReturnOnlyUsersWithTracksAndNonEmptyUsernames() {
        when(spotifyUserLinkRepository.findBySpotifyUser_SpotifyId("sp123"))
                .thenReturn(List.of(link1, link2));

        when(userTrackRepository.existsByUser(user1)).thenReturn(true);
        when(userTrackRepository.existsByUser(user2)).thenReturn(false);

        var result = service.getUsersLinkedToSpotify("sp123");

        assertEquals(1, result.size());
        assertEquals("test_user", result.get(0).get("lastfmUsername"));
    }

    @Test
    void getUsersLinkedToSpotify_shouldReturnEmptyListIfNoLinks() {
        when(spotifyUserLinkRepository.findBySpotifyUser_SpotifyId("sp123"))
                .thenReturn(Collections.emptyList());

        var result = service.getUsersLinkedToSpotify("sp123");
        assertTrue(result.isEmpty());
    }

    @Test
    void unlinkSpotifyAccount_shouldDeleteExistingLink() {
        when(spotifyUserRepository.findBySpotifyId("sp123"))
                .thenReturn(Optional.of(spotifyUser));
        when(userRepository.findByLastfmUsername("test_user"))
                .thenReturn(Optional.of(user1));

        when(spotifyUserLinkRepository.findBySpotifyUserAndUser(spotifyUser, user1))
                .thenReturn(Optional.of(link1));

        service.unlinkSpotifyAccount("sp123", "test_user");

        verify(spotifyUserLinkRepository).delete(link1);
    }

    @Test
    void unlinkSpotifyAccount_shouldThrowIfSpotifyUserNotFound() {
        when(spotifyUserRepository.findBySpotifyId("sp123")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.unlinkSpotifyAccount("sp123", "test_user"));
    }

    @Test
    void unlinkSpotifyAccount_shouldThrowIfLastfmUserNotFound() {
        when(spotifyUserRepository.findBySpotifyId("sp123"))
                .thenReturn(Optional.of(spotifyUser));
        when(userRepository.findByLastfmUsername("test_user"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.unlinkSpotifyAccount("sp123", "test_user"));
    }

    @Test
    void unlinkSpotifyAccount_shouldThrowIfLinkNotFound() {
        when(spotifyUserRepository.findBySpotifyId("sp123"))
                .thenReturn(Optional.of(spotifyUser));
        when(userRepository.findByLastfmUsername("test_user"))
                .thenReturn(Optional.of(user1));
        when(spotifyUserLinkRepository.findBySpotifyUserAndUser(spotifyUser, user1))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.unlinkSpotifyAccount("sp123", "test_user"));
    }
}
