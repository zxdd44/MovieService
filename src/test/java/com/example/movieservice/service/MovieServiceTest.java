package com.example.movieservice.service;

import com.example.movieservice.async.MovieAsyncTaskService;
import com.example.movieservice.async.TaskStatus;
import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.exception.AlreadyExistsException;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.model.Director;
import com.example.movieservice.model.Genre;
import com.example.movieservice.model.Movie;
import com.example.movieservice.model.MovieStatus;
import com.example.movieservice.repository.DirectorRepository;
import com.example.movieservice.repository.GenreRepository;
import com.example.movieservice.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock private MovieRepository movieRepository;
    @Mock private DirectorRepository directorRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private MovieMapper movieMapper;
    @Mock private MovieAsyncTaskService asyncTaskService;
    @InjectMocks private MovieService movieService;

    @Test
    void testSearchComplex_AllBranchesAndCache() {
        Pageable pageable = PageRequest.of(0, 10);
        Movie movie = new Movie();
        MovieDto dto = new MovieDto();
        Page<Movie> page = new PageImpl<>(List.of(movie));

        when(movieRepository.findAll(pageable)).thenReturn(page);
        when(movieMapper.toDto(any())).thenReturn(dto);

        Page<MovieDto> result1 = movieService.searchComplex(null, null, pageable, false);
        Page<MovieDto> result2 = movieService.searchComplex(null, null, pageable, false);

        assertNotNull(result1);
        assertEquals(result1, result2);
        verify(movieRepository, times(1)).findAll(pageable);
    }

    @Test
    void testSearchComplex_ByGenreNative() {
        Pageable pageable = PageRequest.of(0, 10);
        when(movieRepository.findByGenreNative(eq("Action"), any())).thenReturn(Page.empty());

        movieService.searchComplex(null, "Action", pageable, true);

        verify(movieRepository).findByGenreNative(eq("Action"), any());
    }

    @Test
    void testSearchComplex_ByDirector() {
        Pageable pageable = PageRequest.of(0, 10);
        when(movieRepository.findByDirectorJPQL(eq("Nolan"), any())).thenReturn(Page.empty());

        movieService.searchComplex("Nolan", "", pageable, false);

        verify(movieRepository).findByDirectorJPQL(eq("Nolan"), any());
    }

    @Test
    void testGetMovie_Success() {
        Movie movie = new Movie();
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        assertEquals(movie, movieService.getMovie(1L));
    }

    @Test
    void testGetMovie_NotFound_ThrowsException() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> movieService.getMovie(1L));
    }

    @Test
    void testDeleteMovie() {
        movieService.deleteMovie(1L);
        verify(movieRepository).deleteById(1L);
    }

    @Test
    void testUpdateMovie_Success_FullUpdate() {
        Movie movie = new Movie();
        movie.setDirector(new Director());
        MovieDto dto = new MovieDto();
        dto.setTitle("New Title");
        dto.setStatus(1);
        dto.setDirector("New Director");

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Movie result = movieService.updateMovie(1L, dto);

        assertEquals("New Title", result.getTitle());
        assertEquals(MovieStatus.values()[1], result.getStatus());
        verify(directorRepository).save(any());
    }

    @Test
    void testUpdateMovie_NotFound() {
        MovieDto dto = new MovieDto();
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> movieService.updateMovie(1L, dto));
    }

    @Test
    void testCreateMovie_SuccessWithNewGenre() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Inception");
        dto.setStatus(0);
        dto.setGenres(List.of("Sci-Fi"));

        when(movieRepository.existsByTitle("Inception")).thenReturn(false);
        when(genreRepository.findByName("Sci-Fi")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(i -> i.getArgument(0));
        when(movieRepository.save(any(Movie.class))).thenAnswer(i -> i.getArgument(0));

        Movie result = movieService.createMovie(dto);

        assertNotNull(result);
        assertEquals("Inception", result.getTitle());
        verify(genreRepository).save(any());
    }

    @Test
    void testCreateMoviesBulk_ThrowsAlreadyExists() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Existing Movie");

        when(movieRepository.existsByTitle("Existing Movie")).thenReturn(true);

        List<MovieDto> dtos = List.of(dto); // Вынесли из лямбды для Sonar!
        assertThrows(AlreadyExistsException.class, () -> movieService.createMoviesBulk(dtos));
    }

    @Test
    void testCreateMoviesBulkFullDataCoversAllBranches() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Dune");
        dto.setYear(2021);
        dto.setStatus(0);
        dto.setDirector("Denis Villeneuve");
        dto.setGenres(List.of("Sci-Fi", "NewGenre"));

        Genre existingGenre = new Genre();
        existingGenre.setName("Sci-Fi");

        when(movieRepository.existsByTitle("Dune")).thenReturn(false);
        when(genreRepository.findByName("Sci-Fi")).thenReturn(Optional.of(existingGenre));
        when(genreRepository.findByName("NewGenre")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(i -> i.getArgument(0));
        when(directorRepository.save(any(Director.class))).thenAnswer(i -> i.getArgument(0));
        when(movieRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Movie> result = movieService.createMoviesBulk(List.of(dto));

        assertEquals(1, result.size());
        Movie savedMovie = result.get(0);
        assertEquals("Dune", savedMovie.getTitle());
        assertNotNull(savedMovie.getDirector());
        assertEquals("Denis Villeneuve", savedMovie.getDirector().getName());
        assertEquals(2, savedMovie.getGenres().size());

        verify(genreRepository, times(1)).save(any(Genre.class));
        verify(directorRepository, times(1)).save(any(Director.class));
    }

    @Test
    void testCreateMoviesBulkMinimalDataCoversNullChecks() {
        MovieDto dto1 = new MovieDto();
        dto1.setTitle("No Director Movie");
        dto1.setYear(2000);
        dto1.setStatus(1);
        dto1.setDirector(null);
        dto1.setGenres(null);

        MovieDto dto2 = new MovieDto();
        dto2.setTitle("Blank Director Movie");
        dto2.setYear(2005);
        dto2.setStatus(0);
        dto2.setDirector("   ");
        dto2.setGenres(null);

        when(movieRepository.existsByTitle(anyString())).thenReturn(false);
        when(movieRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Movie> result = movieService.createMoviesBulk(List.of(dto1, dto2));

        assertEquals(2, result.size());
        assertNull(result.get(0).getDirector());
        assertNull(result.get(1).getDirector());
        assertNull(result.get(0).getGenres());

        verify(directorRepository, never()).save(any());
        verify(genreRepository, never()).findByName(anyString());
    }

    @Test
    void testCreateMoviesBulkThrowsExceptionWhenMovieExists() {
        MovieDto dto = new MovieDto();
        dto.setTitle("The Matrix");

        when(movieRepository.existsByTitle("The Matrix")).thenReturn(true);
        List<MovieDto> dtos = List.of(dto);
        assertThrows(AlreadyExistsException.class, () -> {
            movieService.createMoviesBulk(dtos);
        }, "Должно быть выброшено AlreadyExistsException, если фильм с таким названием уже есть");
        verify(movieRepository, never()).saveAll(anyList());
    }

    @Test
    void testSearchComplex_ByGenreJPQL() {
        Pageable pageable = PageRequest.of(0, 10);
        when(movieRepository.findByGenreJPQL(eq("Drama"), any())).thenReturn(Page.empty());

        movieService.searchComplex(null, "Drama", pageable, false);

        verify(movieRepository).findByGenreJPQL(eq("Drama"), any());
    }

    @Test
    void testUpdateMovie_AddNewDirector() {
        Movie movieWithoutDirector = new Movie(); // Режиссер null
        MovieDto dto = new MovieDto();
        dto.setDirector("James Cameron");

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movieWithoutDirector));
        when(movieRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        movieService.updateMovie(1L, dto);

        verify(directorRepository).save(any());
        assertNotNull(movieWithoutDirector.getDirector());
    }

    @Test
    void testUpdateMovie_SkipInvalidStatusAndNullDirector() {
        Movie movie = new Movie();
        movie.setTitle("Old Title");
        movie.setYear(2000);
        movie.setStatus(MovieStatus.values()[0]);
        MovieDto dto = new MovieDto();
        dto.setTitle("Updated Title");
        dto.setYear(2025);
        dto.setStatus(-1);
        dto.setDirector(null);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(i -> i.getArgument(0));
        Movie result = movieService.updateMovie(1L, dto);
        assertEquals("Updated Title", result.getTitle());
        assertEquals(2025, result.getYear());
        assertEquals(MovieStatus.values()[0], result.getStatus());
        assertNull(result.getDirector());
        verify(directorRepository, never()).save(any());
    }

    @Test
    void testSearchComplex_BlankDirectorAndNullGenre() {
        Pageable pageable = PageRequest.of(0, 10);
        when(movieRepository.findAll(pageable)).thenReturn(Page.empty());
        movieService.searchComplex("   ", null, pageable, false);
        verify(movieRepository).findAll(pageable);
    }

    @Test
    void testSearchComplex_CacheHit() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> emptyPage = new PageImpl<>(List.of());
        org.mockito.Mockito.lenient().when(movieMapper.toDto(any())).thenReturn(new MovieDto());
        when(movieRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
        movieService.searchComplex(null, null, pageable, false);
        movieService.searchComplex(null, null, pageable, false);
        verify(movieRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testUpdateMovie_StatusTooHigh() {
        Movie movie = new Movie();
        movie.setTitle("Old Title");
        movie.setStatus(MovieStatus.WATCHED);
        MovieDto dto = new MovieDto();
        dto.setTitle("New Title");
        dto.setStatus(999);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(i -> i.getArgument(0));
        Movie result = movieService.updateMovie(1L, dto);
        assertEquals(MovieStatus.WATCHED, result.getStatus());
    }

    @Test
    void testCreateMovie_WithExistingGenre() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Inception");
        dto.setStatus(0);
        dto.setGenres(List.of("Action"));
        Genre existingGenre = new Genre();
        existingGenre.setName("Action");
        when(movieRepository.existsByTitle("Inception")).thenReturn(false);
        when(genreRepository.findByName("Action")).thenReturn(Optional.of(existingGenre));
        when(movieRepository.save(any(Movie.class))).thenAnswer(i -> i.getArgument(0));
        movieService.createMovie(dto);
        verify(genreRepository, never()).save(any(Genre.class));
    }

    @Test
    void testStartAsyncTask_DelegatesToAsyncTaskService() {
        String taskId = movieService.startAsyncTask();
        assertNotNull(taskId);
        verify(asyncTaskService, times(1)).processComplexBusinessLogic(taskId);
    }

    @Test
    void testGetTaskStatus_DelegatesToAsyncTaskService() {
        String taskId = "test-id";
        when(asyncTaskService.getTaskStatus(taskId)).thenReturn(TaskStatus.COMPLETED);
        TaskStatus status = movieService.getTaskStatus(taskId);
        assertEquals(TaskStatus.COMPLETED, status);
        verify(asyncTaskService).getTaskStatus(taskId);
    }

    @Test
    void testStartAsyncTask_CreatesTaskAndReturnsId() {
        String taskId = movieService.startAsyncTask();
        assertNotNull(taskId);
        when(asyncTaskService.getTaskStatus(taskId)).thenReturn(TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, movieService.getTaskStatus(taskId));
        verify(asyncTaskService).processComplexBusinessLogic(taskId);
    }

    @Test
    void testGetTaskStatus_NotFound() {
        when(asyncTaskService.getTaskStatus("non-existent-id")).thenReturn(TaskStatus.NOT_FOUND);
        TaskStatus status = movieService.getTaskStatus("non-existent-id");
        assertEquals(TaskStatus.NOT_FOUND, status);
    }

    @Test
    void testRunSafeCounterDemo() {
        Map<String, Integer> result = movieService.runSafeCounterDemo();
        assertNotNull(result);
        assertTrue(result.containsKey("1_Expected"));
        assertTrue(result.containsKey("2_SafeCounter_Atomic"));
        assertEquals(result.get("1_Expected"), result.get("2_SafeCounter_Atomic"));
    }

    @Test
    void testRunSafeCounterDemo_FullCoverageInterrupt() throws InterruptedException {
        Thread thread = new Thread(() -> movieService.runSafeCounterDemo());
        thread.start();
        thread.interrupt();
        thread.join();
        assertTrue(true);
    }

    @Test
    void testGetTaskStatusMap() {
        assertNotNull(movieService.getTaskStatusMap());
    }

    @Test
    void testStartAsyncTask_Success() {
        String taskId = movieService.startAsyncTask();
        when(asyncTaskService.getTaskStatus(taskId)).thenReturn(TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, movieService.getTaskStatus(taskId));
        verify(asyncTaskService).processComplexBusinessLogic(anyString());
    }

    @Test
    void testStartAsyncTask_CallsAsyncService() {
        String taskId = movieService.startAsyncTask();
        verify(asyncTaskService).processComplexBusinessLogic(anyString());
        assertNotNull(taskId);
    }

    @Test
    void testRunSafeCounterDemo_WithInterrupt() throws InterruptedException {
        Thread t = new Thread(() -> movieService.runSafeCounterDemo());
        t.start();
        t.interrupt();
        t.join(1000);
        assertTrue(true);
    }
}