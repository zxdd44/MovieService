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
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByTitle(String title);
    @Override
    @EntityGraph(attributePaths = {"director", "genres"})
    List<Movie> findAll();

    @Override
    @EntityGraph(attributePaths = {"director", "genres"})
    Optional<Movie> findById(Long id);

    @EntityGraph(attributePaths = {"director", "genres"})
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.name = :genre")
    Page<Movie> findByGenreJPQL(@Param("genre") String genre, Pageable pageable);

    @EntityGraph(attributePaths = {"director", "genres"})
    @Query("SELECT m FROM Movie m JOIN m.director d WHERE d.name = :director")
    Page<Movie> findByDirectorJPQL(@Param("director") String director, Pageable pageable);

    @EntityGraph(attributePaths = {"director", "genres"})
    @Query(value = "SELECT m.* FROM movies m JOIN movie_genres mg ON m.id = mg.movie_id " +
        "JOIN genres g ON mg.genre_id = g.id WHERE g.name = :genre",
        nativeQuery = true)
    Page<Movie> findByGenreNative(@Param("genre") String genre, Pageable pageable);
}
