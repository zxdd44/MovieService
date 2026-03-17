package com.example.movieservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.dto.MovieFilterKey;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.model.Director;
import com.example.movieservice.model.Movie;
import com.example.movieservice.model.MovieStatus;
import com.example.movieservice.repository.DirectorRepository;
import com.example.movieservice.repository.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class MovieService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final MovieMapper movieMapper;

    private final Map<MovieFilterKey, Page<MovieDto>> cache = new HashMap<>();

    public MovieService(MovieRepository movieRepository, DirectorRepository directorRepository,
                        MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.directorRepository = directorRepository;
        this.movieMapper = movieMapper;
    }

    private void invalidateCache() {
        LOGGER.info("Изменение данных! Очистка in-memory индекса...");
        cache.clear();
    }

    // Основной метод поиска с кэшированием
    public Page<MovieDto> searchComplex(String director, String genre, Pageable pageable, boolean useNative) {
        String queryType = useNative ? "NATIVE" : "JPQL";
        MovieFilterKey key = new MovieFilterKey(director, genre,
            pageable.getPageNumber(), pageable.getPageSize(), queryType);

        if (cache.containsKey(key)) {
            LOGGER.info("Данные отданы из кэша (HashMap)!");
            return cache.get(key);
        }

        LOGGER.info("Данных нет в кэше. Выполняем запрос к БД...");
        Page<Movie> moviesPage;
        if (useNative) {
            moviesPage = movieRepository.findByDirectorAndGenreNative(director, genre, pageable);
        } else {
            moviesPage = movieRepository.findByDirectorAndGenreJPQL(director, genre, pageable);
        }

        Page<MovieDto> dtoPage = moviesPage.map(movieMapper::toDto);
        cache.put(key, dtoPage);

        return dtoPage;
    }

    public Movie getMovie(Long id) {
        return movieRepository.findById(id).orElse(null);
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
        Movie savedMovie = movieRepository.save(movie);
        invalidateCache();
        return savedMovie;
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
        Movie updatedMovie = movieRepository.save(movie);
        invalidateCache();
        return updatedMovie;
    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
        invalidateCache();
    }
}