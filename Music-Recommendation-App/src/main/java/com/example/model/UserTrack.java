package com.example.model;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "user_tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTrack {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Track track;

}

