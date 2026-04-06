package com.example.movieservice.mapper;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.model.Movie;
import com.example.movieservice.model.MovieStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovieMapperTest {

    private final MovieMapper mapper = new MovieMapper();

    @Test
    void toDto_ShouldReturnNull_WhenMovieIsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDto_ShouldHandleNullFields() {
        Movie movie = new Movie();
        movie.setTitle("No Director Movie");
        movie.setYear(2025);
        MovieDto dto = mapper.toDto(movie);
        assertNotNull(dto);
        assertEquals("No Director Movie", dto.getTitle());
        assertEquals(0, dto.getStatus());
        assertNull(dto.getDirector());
        assertNull(dto.getGenres());
    }

    @Test
    void toDto_ShouldMapFullMovie() {
        Movie movie = new Movie();
        movie.setTitle("Dune");
        movie.setStatus(MovieStatus.DEFERRED); // Код 2
        MovieDto dto = mapper.toDto(movie);
        assertNotNull(dto);
        assertEquals(2, dto.getStatus());
    }
}