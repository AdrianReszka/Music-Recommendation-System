package com.example.model;

import jakarta.persistence.*;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Track {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String artist;

    private String lastfmId;

    private String spotifyId;

    private String source;

    @ManyToMany
    @JoinTable(
            name = "track_tags",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
