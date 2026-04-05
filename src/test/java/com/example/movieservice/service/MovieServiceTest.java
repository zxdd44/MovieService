package com.example.movieservice.service;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.exception.AlreadyExistsException;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.model.Director;
import com.example.movieservice.model.Genre;
import com.example.movieservice.model.Movie;
import com.example.movieservice.repository.DirectorRepository;
import com.example.movieservice.repository.GenreRepository;
import com.example.movieservice.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    @Test
    void testCreateMoviesBulkFullDataCoversAllBranches() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Dune");
        dto.setYear(2021);
        dto.setStatus(0);
        dto.setDirector("Denis Villeneuve");
        dto.setGenres(List.of("Sci-Fi", "NewGenre"));

        Genre existingGenre = new Genre();
        existingGenre.setName("Sci-Fi");

        when(movieRepository.existsByTitle("Dune")).thenReturn(false);
        when(genreRepository.findByName("Sci-Fi")).thenReturn(Optional.of(existingGenre));
        when(genreRepository.findByName("NewGenre")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(i -> i.getArgument(0));
        when(directorRepository.save(any(Director.class))).thenAnswer(i -> i.getArgument(0));
        when(movieRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Movie> result = movieService.createMoviesBulk(List.of(dto));

        assertEquals(1, result.size());
        Movie savedMovie = result.get(0);
        assertEquals("Dune", savedMovie.getTitle());
        assertNotNull(savedMovie.getDirector());
        assertEquals("Denis Villeneuve", savedMovie.getDirector().getName());
        assertEquals(2, savedMovie.getGenres().size());

        verify(genreRepository, times(1)).save(any(Genre.class));
        verify(directorRepository, times(1)).save(any(Director.class));
    }

    @Test
    void testCreateMoviesBulkMinimalDataCoversNullChecks() {
        MovieDto dto1 = new MovieDto();
        dto1.setTitle("No Director Movie");
        dto1.setYear(2000);
        dto1.setStatus(1);
        dto1.setDirector(null);
        dto1.setGenres(null);

        MovieDto dto2 = new MovieDto();
        dto2.setTitle("Blank Director Movie");
        dto2.setYear(2005);
        dto2.setStatus(0);
        dto2.setDirector("   ");
        dto2.setGenres(null);

        when(movieRepository.existsByTitle(anyString())).thenReturn(false);
        when(movieRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Movie> result = movieService.createMoviesBulk(List.of(dto1, dto2));

        assertEquals(2, result.size());
        assertNull(result.get(0).getDirector());
        assertNull(result.get(1).getDirector());
        assertNull(result.get(0).getGenres());

        verify(directorRepository, never()).save(any());
        verify(genreRepository, never()).findByName(anyString());
    }

    @Test
    void testCreateMoviesBulkThrowsExceptionWhenMovieExists() {
        MovieDto dto = new MovieDto();
        dto.setTitle("The Matrix");

        when(movieRepository.existsByTitle("The Matrix")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> movieService.createMoviesBulk(List.of(dto)));
        verify(movieRepository, never()).saveAll(anyList());
    }
}