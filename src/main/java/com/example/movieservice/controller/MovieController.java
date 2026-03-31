package com.example.movieservice.controller;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.service.MovieService;
import com.example.movieservice.model.Movie;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Управление фильмами", description = "API для поиска, создания и редактирования фильмов")
public class MovieController {
    private final MovieService movieService;
    private final MovieMapper movieMapper;

    public MovieController(MovieService movieService, MovieMapper movieMapper) {
        this.movieService = movieService;
        this.movieMapper = movieMapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить фильм по ID")
    public MovieDto getById(@PathVariable Long id) {
        return movieMapper.toDto(movieService.getMovie(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск фильмов с пагинацией и фильтрами")
    public Page<MovieDto> searchMovies(
        @RequestParam(required = false) String director,
        @RequestParam(required = false) String genre,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "false") boolean useNative) {

        Pageable pageable = PageRequest.of(page, size);
        return movieService.searchComplex(director, genre, pageable, useNative);
    }

    @PostMapping
    @Operation(summary = "Создать новый фильм")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
        description = "Фильм с таким названием уже существует")
    public MovieDto createMovie(@Valid @RequestBody MovieDto movieDto) {
        Movie savedMovie = movieService.createMovie(movieDto);
        return movieMapper.toDto(savedMovie);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить существующий фильм")
    public MovieDto updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDto movieDto) {
        Movie updatedMovie = movieService.updateMovie(id, movieDto);
        return movieMapper.toDto(updatedMovie);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить фильм")
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }
}