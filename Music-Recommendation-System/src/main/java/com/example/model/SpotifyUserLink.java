package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spotify_user_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotifyUserLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "spotify_user_id")
    private SpotifyUser spotifyUser;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
