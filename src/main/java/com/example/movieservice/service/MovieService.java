package com.example.movieservice.service;

import com.example.movieservice.async.MovieAsyncTaskService;
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
import com.example.movieservice.model.Genre;
import com.example.movieservice.repository.GenreRepository;
import com.example.movieservice.exception.AlreadyExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.movieservice.async.TaskStatus;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class MovieService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final MovieMapper movieMapper;
    private final GenreRepository genreRepository;
    private final Map<MovieFilterKey, Page<MovieDto>> cache = new HashMap<>();
    private int syncCounter = 0;
    private final AtomicInteger safeCounter = new AtomicInteger(0);
    private final MovieAsyncTaskService asyncTaskService;

    public MovieService(MovieRepository movieRepository, DirectorRepository directorRepository,
                        MovieMapper movieMapper, GenreRepository genreRepository,
                        MovieAsyncTaskService asyncTaskService) {
        this.movieRepository = movieRepository;
        this.directorRepository = directorRepository;
        this.movieMapper = movieMapper;
        this.genreRepository = genreRepository;
        this.asyncTaskService = asyncTaskService;
    }

    public String startAsyncTask() {
        String taskId = UUID.randomUUID().toString();
        asyncTaskService.processComplexBusinessLogic(taskId);
        return taskId;
    }

    public TaskStatus getTaskStatus(String taskId) {
        return asyncTaskService.getTaskStatus(taskId);
    }

    private synchronized void incrementSync() {
        syncCounter++;
    }

    public Map<String, Integer> runSafeCounterDemo() {
        safeCounter.set(0);
        syncCounter = 0;
        int threadsCount = 100;
        int iterationsPerThread = 10000;

        try (ExecutorService executor = Executors.newFixedThreadPool(threadsCount)) {
            for (int i = 0; i < threadsCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        safeCounter.incrementAndGet();
                        incrementSync();
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Integer> results = new HashMap<>();
        results.put("1_Expected", threadsCount * iterationsPerThread);
        results.put("2_SafeCounter_Atomic", safeCounter.get());
        results.put("3_Safe_Synchronized", syncCounter);

        return results;
    }

    private void invalidateCache() {
        LOGGER.info("Изменение данных! Очистка in-memory индекса...");
        cache.clear();
    }

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

        if (genre != null && !genre.isBlank()) {
            moviesPage = useNative ?
                movieRepository.findByGenreNative(genre, pageable) :
                movieRepository.findByGenreJPQL(genre, pageable);
        } else if (director != null && !director.isBlank()) {
            moviesPage = movieRepository.findByDirectorJPQL(director, pageable);
        } else {
            moviesPage = movieRepository.findAll(pageable);
        }

        Page<MovieDto> dtoPage = moviesPage.map(movieMapper::toDto);
        cache.put(key, dtoPage);

        return dtoPage;
    }

    public Movie getMovie(Long id) {
        return movieRepository.findById(id)
            .orElseThrow(() -> {
                LOGGER.error("Фильм с ID {} не найден в базе данных!", id);
                return new RuntimeException("Фильм не найден");
            });
    }

    @Transactional
    public Movie createMovie(MovieDto dto) {
        Movie movie = convertToEntity(dto);
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

    @Transactional
    public List<Movie> createMoviesBulk(List<MovieDto> dtos) {
        LOGGER.info("Начало массового импорта {} фильмов", dtos.size());
        List<Movie> moviesToSave = dtos.stream()
            .map(this::convertToEntity)
            .toList();
        List<Movie> savedMovies = movieRepository.saveAll(moviesToSave);
        invalidateCache();
        LOGGER.info("Успешно импортировано {} фильмов", savedMovies.size());
        return savedMovies;
    }

    private Movie convertToEntity(MovieDto dto) {
        if (movieRepository.existsByTitle(dto.getTitle())) {
            throw new AlreadyExistsException("Фильм с таким названием уже существует!");
        }

        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setYear(dto.getYear());
        movie.setStatus(MovieStatus.values()[dto.getStatus()]);

        Optional.ofNullable(dto.getDirector())
            .filter(name -> !name.trim().isEmpty())
            .ifPresent(name -> {
                Director director = new Director();
                director.setName(name);
                directorRepository.save(director);
                movie.setDirector(director);
            });

        if (dto.getGenres() != null) {
            Set<Genre> movieGenres = dto.getGenres().stream()
                .map(name -> genreRepository.findByName(name)
                    .orElseGet(() -> {
                        Genre newGenre = new Genre();
                        newGenre.setName(name);
                        return genreRepository.save(newGenre);
                    }))
                .collect(Collectors.toSet());
            movie.setGenres(movieGenres);
        }
        return movie;
    }
}