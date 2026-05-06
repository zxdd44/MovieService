package com.example.movieservice.controller;

import com.example.movieservice.async.TaskStatus;
import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.dto.ReviewDTO;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.repository.UserMovieProgressRepository;
import com.example.movieservice.service.MovieService;
import com.example.movieservice.model.Movie;
import com.example.movieservice.model.MovieStatus;
import com.example.movieservice.model.UserMovieProgress;
import com.example.movieservice.repository.MovieRepository;
import com.example.movieservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import com.example.movieservice.dto.UserProfileDto;
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
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Управление фильмами", description = "API для поиска, создания и редактирования фильмов")
public class MovieController {
    private final MovieService movieService;
    private final MovieMapper movieMapper;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final UserMovieProgressRepository progressRepository;

    public MovieController(MovieService movieService, MovieMapper movieMapper,
                           MovieRepository movieRepository, UserRepository userRepository,
                           UserMovieProgressRepository progressRepository) {
        this.movieService = movieService;
        this.movieMapper = movieMapper;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
    }

    @PostMapping("/bulk")
    @Operation(summary = "Массовое создание фильмов", description = "Принимает список DTO и сохраняет их в базу")
    public ResponseEntity<List<Movie>> createMoviesBulk(@RequestBody List<MovieDto> dtos) {
        List<Movie> createdMovies = movieService.createMoviesBulk(dtos);
        return ResponseEntity.ok(createdMovies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить фильм по ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
        description = "Фильм не найден")
    public MovieDto getById(@PathVariable Long id, Principal principal) {
        Movie movie = movieService.getMovie(id);
        MovieDto dto = movieMapper.toDto(movie);
        if (principal != null) {
            int personalStatus = movieService.getPersonalMovieStatus(id, principal.getName());
            dto.setStatus(personalStatus);
        } else {
            dto.setStatus(0);
        }
        return movieMapper.toDto(movie);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> addReview(@PathVariable Long id, @RequestBody ReviewDTO reviewDto) {
        movieService.addReview(id, reviewDto.getUserId(), reviewDto.getContent(), reviewDto.getRating());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
        @PathVariable Long reviewId,
        Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Необходимо авторизоваться");
        }
        movieService.deleteReview(reviewId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск фильмов с пагинацией и фильтрами")
    public Page<MovieDto> searchMovies(
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String director,
        @RequestParam(required = false) String genre,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "false") boolean useNative) {
        Pageable pageable = PageRequest.of(page, size);
        return movieService.searchComplex(title, director, genre, pageable, useNative);
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
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{id}/public-profile")
    public ResponseEntity<?> getPublicProfile(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            List<UserMovieProgress> progressList = progressRepository.findByUserId(id);
            long watched = progressList.stream().filter(p -> p.getStatus() == MovieStatus.WATCHED).count();
            long dropped = progressList.stream().filter(p -> p.getStatus() == MovieStatus.ABANDONED).count();
            long planned = progressList.stream().filter(p -> p.getStatus() == MovieStatus.DEFERRED).count();
            long total = watched + dropped + planned;
            Map<String, UserProfileDto.ChartData> chart = new HashMap<>();
            chart.put("WATCHED", new UserProfileDto.ChartData(watched, total == 0 ? 0 : (watched * 100.0) / total));
            chart.put("ABANDONED", new UserProfileDto.ChartData(dropped, total == 0 ? 0 : (dropped * 100.0) / total));
            chart.put("DEFERRED", new UserProfileDto.ChartData(planned, total == 0 ? 0 : (planned * 100.0) / total));
            UserProfileDto profile = new UserProfileDto();
            profile.setUsername(user.getUsername());
            profile.setStatus(user.getStatus());
            profile.setAvatarUrl(user.getAvatarUrl());
            profile.setStatusChart(chart);
            List<MovieDto> watchedMovies = progressList.stream()
                .filter(p -> p.getStatus() == MovieStatus.WATCHED)
                .map(p -> movieMapper.toDto(p.getMovie()))
                .limit(10)
                .toList();
            profile.setWatchedMovies(watchedMovies);
            return ResponseEntity.ok(profile);
        }).orElse(ResponseEntity.status(404).body(null));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Удаление пользователя (только для админа)")
    public ResponseEntity<?> deleteUser(
        @PathVariable Long userId,
        Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Необходимо авторизоваться");
        }
        movieService.deleteUser(userId, principal.getName());
        return ResponseEntity.ok("Пользователь и все его данные успешно удалены");
    }

    @PostMapping("/async/start")
    @Operation(summary = "Запуск долгой фоновой задачи")
    public ResponseEntity<Map<String, String>> startAsyncTask() {
        String taskId = movieService.startAsyncTask();
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", TaskStatus.IN_PROGRESS.name());
        response.put("message", "Задача успешно запущена и выполняется в фоновом режиме");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/async/status/{taskId}")
    @Operation(summary = "Проверить статус задачи")
    public ResponseEntity<Map<String, String>> checkTaskStatus(@PathVariable String taskId) {
        TaskStatus status = movieService.getTaskStatus(taskId);
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", status.name());
        String message = switch (status) {
            case IN_PROGRESS -> "Задача всё еще выполняется. Подождите немного.";
            case COMPLETED -> "Задача успешно завершена.";
            case ERROR -> "Произошла ошибка при выполнении задачи.";
            default -> "Задача с таким ID не найдена.";
        };
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/race-condition-solved")
    @Operation(summary = "Демонстрация потокобезопасного счетчика", description =
        "Запускает 50 потоков, которые безопасно увеличивают Atomic счетчик")
    public ResponseEntity<Map<String, Integer>> demoRaceConditionSolved() {
        return ResponseEntity.ok(movieService.runSafeCounterDemo());
    }

    @PutMapping("/{id}/progress")
    @Operation(summary = "Обновить личный прогресс просмотра фильма")
    public ResponseEntity<?> updateMovieProgress(
        @PathVariable Long id,
        @RequestParam int status,
        Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Нужно авторизоваться");
        }
        movieService.updatePlayerProgress(id, principal.getName(), status);
        return ResponseEntity.ok().build();
    }
}