package com.example.movieservice.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityCoverageTest {

    @Test
    void testMovieEntity() {
        Movie movie = new Movie("Test", 2000, MovieStatus.NOT_WATCHED, new Director());
        movie.setTitle("New Title");
        movie.setYear(2026);
        movie.setStatus(MovieStatus.ABANDONED); // Твой статус
        movie.setDirector(new Director());
        movie.setGenres(Set.of(new Genre()));
        movie.setReviews(List.of(new Review()));
        movie.setStudio(new Studio());

        assertNull(movie.getId()); // ID обычно null до сохранения в БД
        assertEquals("New Title", movie.getTitle());
        assertEquals(2026, movie.getYear());
        assertEquals(MovieStatus.ABANDONED, movie.getStatus());
        assertNotNull(movie.getDirector());
        assertNotNull(movie.getGenres());
        assertNotNull(movie.getReviews());
        assertNotNull(movie.getStudio());
    }

    @Test
    void testDirectorEntity() {
        Director director = new Director();
        director.setId(1L);
        director.setName("Tarantino");

        assertEquals(1L, director.getId());
        assertEquals("Tarantino", director.getName());
    }

    @Test
    void testGenreEntity() {
        Genre genre = new Genre();
        genre.setName("Comedy");
        assertEquals("Comedy", genre.getName());
    }

    @Test
    void testReviewEntity() {
        Review review = new Review();
        review.setContent("Awesome!");
        review.setMovie(new Movie());

        assertEquals("Awesome!", review.getContent());
    }

    @Test
    void testStudioEntity() {
        Studio studio = new Studio();
        studio.setName("Universal");
        assertEquals("Universal", studio.getName());
    }

    @Test
    void testMovieStatusEnum() {
        // Покрытие Enum
        assertEquals(0, MovieStatus.NOT_WATCHED.getCode());
        assertEquals(1, MovieStatus.WATCHED.getCode());
        assertEquals(2, MovieStatus.DEFERRED.getCode());
        assertEquals(3, MovieStatus.ABANDONED.getCode());

        MovieStatus[] values = MovieStatus.values();
        assertEquals(4, values.length);
        assertEquals(MovieStatus.NOT_WATCHED, MovieStatus.valueOf("NOT_WATCHED"));
    }
}