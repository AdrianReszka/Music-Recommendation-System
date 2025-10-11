package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String lastfmUsername;

    @Column(length = 4000)
    private String spotifyToken;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<SpotifyUserLink> links = new HashSet<>();
}

