package com.example.model;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "playlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private String spotifyId;

    private String name;

}

