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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class DemoService {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DemoService.class);
    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;
    private int unsafeCounter = 0;

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

    public Map<String, Integer> runUnsafeRaceConditionDemo() {
        unsafeCounter = 0;
        int threadsCount = 50;
        int iterationsPerThread = 2000;

        try (ExecutorService executor = Executors.newFixedThreadPool(threadsCount)) {
            for (int i = 0; i < threadsCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        unsafeCounter++;
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Поток был прерван во время демонстрации race condition", e);
        }

        Map<String, Integer> results = new HashMap<>();
        results.put("1_Expected", threadsCount * iterationsPerThread);
        results.put("2_UnsafeCounter_Result", unsafeCounter);
        return results;
    }
}
