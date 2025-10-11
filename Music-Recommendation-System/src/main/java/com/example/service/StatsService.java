package com.example.service;

import com.example.model.Stats;
import com.example.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class StatsService {

    private final StatsRepository statsRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final UserTrackRepository userTrackRepository;
    private final SpotifyUserLinkRepository spotifyUserLinkRepository;

    private static final long STATS_ID = 1L;

    @PostConstruct
    public void init() {
        if (statsRepository.count() == 0) {
            Stats stats = new Stats();
            stats.setTotalTracks(0);
            stats.setAverageTracksPerUser(0);
            stats.setTotalLinkedAccounts(0);
            statsRepository.save(stats);
        }
    }

    @Transactional
    public void updateIfIncreased() {
        Stats stats = statsRepository.findById(STATS_ID).orElseThrow();

        long totalUserTracks = userTrackRepository.count();
        long totalLinkedAccounts = spotifyUserLinkRepository.count();
        long totalUsers = userRepository.count();

        double currentAverage = totalUsers > 0 ? (double) totalUserTracks / totalUsers : 0;

        boolean updated = false;

        if (totalUserTracks > stats.getTotalTracks()) {
            stats.setTotalTracks(totalUserTracks);
            updated = true;
        }
        if (totalLinkedAccounts > stats.getTotalLinkedAccounts()) {
            stats.setTotalLinkedAccounts(totalLinkedAccounts);
            updated = true;
        }
        if (currentAverage > stats.getAverageTracksPerUser()) {
            stats.setAverageTracksPerUser(currentAverage);
            updated = true;
        }

        if (updated) {
            statsRepository.save(stats);
            System.out.printf("✅ Stats updated → tracks=%d, avg=%.2f, linked=%d%n",
                    stats.getTotalTracks(), stats.getAverageTracksPerUser(), stats.getTotalLinkedAccounts());
        } else {
            System.out.println("ℹ️ No stat changes detected — skipping update.");
        }
    }

    public Stats getStats() {
        return statsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Stats record not found"));
    }
}
