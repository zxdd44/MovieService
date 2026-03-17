package com.example.movieservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.movieservice.model.Director;
import com.example.movieservice.model.Movie;
import com.example.movieservice.dto.MovieDto;
import org.springframework.stereotype.Service;
import com.example.movieservice.repository.DirectorRepository;
import com.example.movieservice.repository.MovieRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DemoService {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DemoService.class);
    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;

    public DemoService(DirectorRepository directorRepository, MovieRepository movieRepository) {
        this.directorRepository = directorRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional
    public Movie createWithTransaction(MovieDto dto) {
        Director director = new Director();
        director.setName(dto.getDirector());
        directorRepository.save(director);
        throw new MovieServiceException("Ошибка! Благодаря @Transactional режиссер НЕ сохранится в БД.");
    }

    public Movie createWithoutTransaction(MovieDto dto) {
        Director director = new Director();
        director.setName(dto.getDirector());
        directorRepository.save(director);
        throw new MovieServiceException("Ошибка! Но @Transactional нет, поэтому режиссер ОСТАНЕТСЯ в БД.");
    }

    public void demonstrateNPO() {
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            if (movie.getDirector() != null) {
                LOGGER.info("Фильм: " + movie.getTitle() +
                    ", Режиссер: " + movie.getDirector().getName());
            }
        }
    }
}
