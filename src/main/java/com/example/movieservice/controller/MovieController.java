package com.example.movieservice.controller;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.service.MovieService;
import com.example.movieservice.model.Movie;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/filter") // RequestParam: /api/movies/filter?director=Nolan
    public List<MovieDto> getByDirector(@RequestParam String director) {
        return movieService.searchByDirector(director).stream()
            .map(movieMapper::toDto)
            .toList();
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