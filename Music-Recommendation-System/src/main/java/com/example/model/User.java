package com.example.model;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String lastfmUsername;

    private String spotifyId;

    @Column(length = 4000)
    private String spotifyToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spotify_user_id")
    private SpotifyUser spotifyUser;

}
