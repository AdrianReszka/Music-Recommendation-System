package com.example.model;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Track track;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}

