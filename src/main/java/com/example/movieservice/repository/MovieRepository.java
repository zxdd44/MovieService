package com.example.movieservice.repository;

import com.example.movieservice.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Override
    @EntityGraph(attributePaths = {"director", "genres"})
    List<Movie> findAll();

    @Query("SELECT m FROM Movie m JOIN m.director d JOIN m.genres g WHERE d.name = :director AND g.name = :genre")
    Page<Movie> findByDirectorAndGenreJPQL(@Param("director") String director,
                                           @Param("genre") String genre, Pageable pageable);

    @Query(value = "SELECT m.* FROM movies m " +
        "JOIN directors d ON m.director_id = d.id " +
        "JOIN movie_genres mg ON m.id = mg.movie_id " +
        "JOIN genres g ON mg.genre_id = g.id " +
        "WHERE d.name = :director AND g.name = :genre",
        nativeQuery = true)
    Page<Movie> findByDirectorAndGenreNative(@Param("director") String director,
                                             @Param("genre") String genre, Pageable pageable);
}
