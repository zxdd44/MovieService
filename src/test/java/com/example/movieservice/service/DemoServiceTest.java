package com.example.movieservice.service;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.exception.AlreadyExistsException;
import com.example.movieservice.model.Director;
import com.example.movieservice.model.Movie;
import com.example.movieservice.repository.DirectorRepository;
import com.example.movieservice.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DemoServiceTest {

    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private DemoService demoService;

    @Test
    void testCreateWithTransaction_ThrowsException() {
        MovieDto dto = new MovieDto();
        dto.setDirector("Christopher Nolan");

        MovieServiceException exception = assertThrows(MovieServiceException.class, () -> {
            demoService.createWithTransaction(dto);
        });

        assertEquals("Ошибка! Благодаря @Transactional режиссер НЕ сохранится в БД.", exception.getMessage());
        verify(directorRepository).save(any(Director.class));
    }

    @Test
    void testCreateWithoutTransaction_ThrowsException() {
        MovieDto dto = new MovieDto();
        dto.setDirector("Quentin Tarantino");

        MovieServiceException exception = assertThrows(MovieServiceException.class, () -> {
            demoService.createWithoutTransaction(dto);
        });

        assertEquals("Ошибка! Но @Transactional нет, поэтому режиссер ОСТАНЕТСЯ в БД.", exception.getMessage());
        verify(directorRepository).save(any(Director.class));
    }

    @Test
    void testDemonstrateNPO_CoversAllBranches() {
        Movie movieWithDirector = new Movie();
        movieWithDirector.setTitle("Inception");
        Director director = new Director();
        director.setName("Christopher Nolan");
        movieWithDirector.setDirector(director);
        Movie movieWithoutDirector = new Movie();
        movieWithoutDirector.setTitle("Unknown Movie");
        movieWithoutDirector.setDirector(null);
        when(movieRepository.findAll()).thenReturn(List.of(movieWithDirector, movieWithoutDirector));
        demoService.demonstrateNPO();
        verify(movieRepository).findAll();
    }

    @Test
    void testCreateBulkWithoutTransaction_PartialSuccess() {
        MovieDto validDto = new MovieDto();
        validDto.setTitle("Matrix");
        validDto.setDirector("Wachowski");
        MovieDto duplicateDto = new MovieDto();
        duplicateDto.setTitle("Interstellar");
        when(movieRepository.existsByTitle("Matrix")).thenReturn(false);
        when(movieRepository.existsByTitle("Interstellar")).thenReturn(true);
        List<MovieDto> dtos = List.of(validDto, duplicateDto);
        assertThrows(AlreadyExistsException.class, () -> {
            demoService.createBulkWithoutTransaction(dtos);
        });
        verify(movieRepository, times(1)).save(any(Movie.class));
        verify(directorRepository, times(1)).save(any(Director.class));
    }
}