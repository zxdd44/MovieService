package com.example.movieservice.controller;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.service.MovieService;
import com.example.movieservice.model.Movie;

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
public class MovieController {
    private final MovieService movieService;
    private final MovieMapper movieMapper;

    public MovieController(MovieService movieService, MovieMapper movieMapper) {
        this.movieService = movieService;
        this.movieMapper = movieMapper;
    }

    @GetMapping("/{id}") // PathVariable: /api/movies/1
    public MovieDto getById(@PathVariable Long id) {
        return movieMapper.toDto(movieService.getMovie(id));
    }

    // GET /api/movies/search?director=Nolan&genre=Sci-Fi&page=0&size=5&useNative=false
    @GetMapping("/search")
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
    public MovieDto createMovie(@RequestBody MovieDto movieDto) {
        Movie savedMovie = movieService.createMovie(movieDto);
        return movieMapper.toDto(savedMovie);
    }

    @PutMapping("/{id}")
    public MovieDto updateMovie(@PathVariable Long id, @RequestBody MovieDto movieDto) {
        Movie updatedMovie = movieService.updateMovie(id, movieDto);
        return movieMapper.toDto(updatedMovie);
    }

    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }
}