package com.example.movieservice.service;

import com.example.movieservice.model.Director;
import com.example.movieservice.model.Movie;
import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.model.MovieStatus;
import com.example.movieservice.repository.DirectorRepository;
import com.example.movieservice.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;

    public MovieService(MovieRepository movieRepository, DirectorRepository directorRepository) {
        this.movieRepository = movieRepository;
        this.directorRepository = directorRepository;
    }

    public Movie getMovie(Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    public List<Movie> searchByDirector(String name) {
        return movieRepository.findAll().stream()
            .filter(
                m -> m.getDirector() != null && m.getDirector().getName().equalsIgnoreCase(name))
            .toList();
    }

    @Transactional
    public Movie createMovie(MovieDto dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setYear(dto.getYear());
        movie.setStatus(MovieStatus.values()[dto.getStatus()]);
        if (dto.getDirector() != null && !dto.getDirector().isBlank()) {
            Director director = new Director();
            director.setName(dto.getDirector());
            directorRepository.save(director);
            movie.setDirector(director);
        }
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie updateMovie(Long id, MovieDto dto) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Фильм не найден"));
        movie.setTitle(dto.getTitle());
        movie.setYear(dto.getYear());
        if (dto.getStatus() >= 0 && dto.getStatus() < MovieStatus.values().length) {
            movie.setStatus(MovieStatus.values()[dto.getStatus()]);
        }
        if (dto.getDirector() != null) {
            Director director = movie.getDirector();
            if (director == null) {
                director = new Director();
            }
            director.setName(dto.getDirector());
            directorRepository.save(director);
            movie.setDirector(director);
        }
        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
}