package com.example.movieservice.service;

import com.example.movieservice.async.MovieAsyncTaskService;
import com.example.movieservice.repository.ReviewRepository;
import com.example.movieservice.repository.UserRepository;
import org.slf4j.Logger;
import com.example.movieservice.model.UserMovieProgress;
import com.example.movieservice.repository.UserMovieProgressRepository;
import org.slf4j.LoggerFactory;
import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.dto.MovieFilterKey;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.model.Director;
import com.example.movieservice.model.Movie;
import com.example.movieservice.model.User;
import com.example.movieservice.model.Review;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class MovieService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DirectorRepository directorRepository;
    private final MovieMapper movieMapper;
    private final GenreRepository genreRepository;
    private final Map<MovieFilterKey, Page<MovieDto>> cache = new ConcurrentHashMap<>();
    private int syncCounter = 0;
    private final AtomicInteger safeCounter = new AtomicInteger(0);
    private final MovieAsyncTaskService asyncTaskService;
    private final UserMovieProgressRepository progressRepository;

    public MovieService(MovieRepository movieRepository, ReviewRepository reviewRepository,
                        DirectorRepository directorRepository, UserRepository userRepository,
                        MovieMapper movieMapper, GenreRepository genreRepository,
                        MovieAsyncTaskService asyncTaskService, UserMovieProgressRepository progressRepository) {
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
        this.directorRepository = directorRepository;
        this.movieMapper = movieMapper;
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
        this.asyncTaskService = asyncTaskService;
        this.progressRepository = progressRepository;
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

    public Page<MovieDto> searchComplex(String title, String director, String genre,
                                        Pageable pageable, boolean useNative) {
        MovieFilterKey key = new MovieFilterKey(title, director, genre,
            pageable.getPageNumber(), pageable.getPageSize(), useNative ? "NATIVE" : "JPQL");

        if (cache.containsKey(key)) {
            LOGGER.info("Данные отданы из кэша (HashMap)!");
            return cache.get(key);
        }

        LOGGER.info("Данных нет в кэше. Выполняем запрос к БД...");
        Page<Movie> moviesPage;

        if (title != null && !title.isBlank()) {
            moviesPage = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (genre != null && !genre.isBlank()) {
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

    private Set<Genre> handleGenres(List<String> genreNames) {
        if (genreNames == null) {
            return Set.of();
        }
        return genreNames.stream()
            .map(name -> genreRepository.findByName(name)
                .orElseGet(() -> {
                    Genre newGenre = new Genre();
                    newGenre.setName(name);
                    return genreRepository.save(newGenre);
                }))
            .collect(Collectors.toSet());
    }

    @Transactional
    public Movie updateMovie(Long id, MovieDto dto) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Фильм не найден"));
        movie.setTitle(dto.getTitle());
        movie.setYear(dto.getYear());
        movie.setImageUrl(dto.getImageUrl());

        if (dto.getStatus() >= 0 && dto.getStatus() < MovieStatus.values().length) {
            movie.setStatus(MovieStatus.values()[dto.getStatus()]);
        }

        if (dto.getDirector() != null) {
            String directorName = dto.getDirector().trim();
            Director director = directorRepository.findByName(directorName)
                .orElseGet(() -> {
                    Director d = new Director();
                    d.setName(directorName);
                    return directorRepository.save(d);
                });
            movie.setDirector(director);
        }

        movie.setGenres(handleGenres(dto.getGenres()));

        Movie updatedMovie = movieRepository.save(movie);
        invalidateCache();
        return updatedMovie;
    }

    @Transactional
    public void updatePlayerProgress(Long movieId, String username, int statusIndex) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new RuntimeException("Фильм не найден"));
        UserMovieProgress progress = progressRepository.findByUserIdAndMovieId(user.getId(), movie.getId())
            .orElseGet(() -> {
                UserMovieProgress newProgress = new UserMovieProgress();
                newProgress.setUser(user);
                newProgress.setMovie(movie);
                return newProgress;
            });
        if (statusIndex >= 0 && statusIndex < MovieStatus.values().length) {
            progress.setStatus(MovieStatus.values()[statusIndex]);
        }
        progressRepository.saveAndFlush(progress);
    }

    private void updateAverageRating(Movie movie) {
        if (movie.getReviews() == null || movie.getReviews().isEmpty()) {
            movie.setAverageRating(0.0);
            return;
        }
        double avg = movie.getReviews().stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        movie.setAverageRating(avg);
    }

    @Transactional
    public void deleteReview(Long reviewId, String currentUsername) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Отзыв не найден"));
        User currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Movie movie = review.getMovie();
        boolean isOwner = review.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        if (isOwner || isAdmin) {
            reviewRepository.delete(review);
            movie.getReviews().remove(review);
            updateAverageRating(movie);
            movieRepository.save(movie);
        } else {
            throw new RuntimeException("Вы не можете удалить чужой отзыв!");
        }
    }

    public int getPersonalMovieStatus(Long movieId, String username) {
        return userRepository.findByUsername(username)
            .flatMap(user -> progressRepository.findByUserIdAndMovieId(user.getId(), movieId))
            .map(progress -> progress.getStatus().ordinal())
            .orElse(0);
    }

    @Transactional
    public void addReview(Long movieId, Long userId, String text, int score) {
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        Review review = new Review();
        review.setContent(text);
        review.setRating(score);
        review.setMovie(movie);
        review.setUser(user);
        reviewRepository.save(review);
        movie.getReviews().add(review);
        updateAverageRating(movie);
        movieRepository.save(movie);
        invalidateCache();
    }

    @Transactional
    public void deleteUser(Long userIdToDelete, String requesterUsername) {
        User requester = userRepository.findByUsername(requesterUsername)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isAdmin = "ROLE_ADMIN".equals(requester.getRole());
        boolean isSelf = requester.getId().equals(userIdToDelete);

        if (!isAdmin && !isSelf) {
            throw new RuntimeException("У вас нет прав для удаления других пользователей!");
        }

        if (isAdmin && isSelf) {
            throw new RuntimeException("Вы не можете удалить свою собственную учетную запись администратора!");
        }

        if (!userRepository.existsById(userIdToDelete)) {
            throw new RuntimeException("Пользователь не найден");
        }

        userRepository.deleteById(userIdToDelete);
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

        movie.setGenres(handleGenres(dto.getGenres()));
        return movie;
    }
}