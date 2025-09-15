package com.example.repository;

import com.example.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    Optional<Track> findByTitleAndArtist(String title, String artist);

    List<Track> findByTags_NameIn(Collection<String> tagNames);

    @Query("SELECT DISTINCT t FROM Track t LEFT JOIN FETCH t.tags WHERE t.id IN :ids")
    List<Track> findAllWithTagsByIdIn(@Param("ids") List<Long> ids);
}