package com.example.movieservice.service;

import com.example.movieservice.exception.AlreadyExistsException;
import com.example.movieservice.model.MovieStatus;
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
                LOGGER.info("Фильм: {}, Режиссер: {}", movie.getTitle(), movie.getDirector().getName());
            }
        }
    }

    public void createBulkWithoutTransaction(List<MovieDto> dtos) {
        for (MovieDto dto : dtos) {
            String sanitizedTitle = dto.getTitle() != null
                ? dto.getTitle().replaceAll("[\n\r\t]", "_")
                : "null";
            LOGGER.info("Обработка фильма: {}", sanitizedTitle);
            if (movieRepository.existsByTitle(dto.getTitle())) {
                LOGGER.error("Ошибка: Фильм '{}' уже существует!", sanitizedTitle);
                throw new AlreadyExistsException("Фильм с названием '" + sanitizedTitle + "' уже существует!");
            }
            Movie movie = new Movie();
            movie.setTitle(dto.getTitle());
            movie.setYear(dto.getYear());
            if (dto.getStatus() >= 0 && dto.getStatus() < MovieStatus.values().length) {
                movie.setStatus(MovieStatus.values()[dto.getStatus()]);
            }
            if (dto.getDirector() != null && !dto.getDirector().isBlank()) {
                Director director = new Director();
                director.setName(dto.getDirector());
                directorRepository.save(director);
                movie.setDirector(director);
            }
            movieRepository.save(movie);
            LOGGER.info("Фильм '{}' успешно сохранен в БД", sanitizedTitle);
        }
    }
}
