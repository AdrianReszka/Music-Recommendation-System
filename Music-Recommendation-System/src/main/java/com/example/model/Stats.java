package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long totalTracks;

    @Column(nullable = false)
    private double averageTracksPerUser;

    @Column(nullable = false)
    private long totalLinkedAccounts;
}
