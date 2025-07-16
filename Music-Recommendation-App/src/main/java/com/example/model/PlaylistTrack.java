package com.example.model;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "playlist_tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistTrack {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Playlist playlist;

    @ManyToOne
    private Track track;

    private int position;
}

