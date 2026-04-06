package com.example.movieservice.controller;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.exception.AlreadyExistsException;
import com.example.movieservice.exception.GlobalExceptionHandler;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.model.Movie;
import com.example.movieservice.model.MovieStatus;
import com.example.movieservice.service.MovieService;
import com.example.movieservice.service.MovieServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {MovieController.class, GlobalExceptionHandler.class})
class MovieControllerIntegrationTest {

    private static final String API_MOVIES = "/api/movies";
    private static final String ERROR_PATH = "$.error";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    @MockBean
    private MovieMapper movieMapper;

    // Вспомогательный метод для создания валидного DTO
    private MovieDto createValidDto() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Valid Title");
        dto.setYear(2024);
        dto.setDirector("Some Director");
        return dto;
    }

    @Test
    void searchMovies_ShouldReturnPage() throws Exception {
        MovieDto dto = createValidDto();
        when(movieService.searchComplex(any(), any(), any(Pageable.class), anyBoolean()))
            .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get(API_MOVIES + "/search?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Valid Title"));
    }

    @Test
    void createMovie_ShouldReturnSavedMovie() throws Exception {
        MovieDto inputDto = createValidDto();
        Movie savedMovie = new Movie();
        savedMovie.setTitle("Valid Title");
        savedMovie.setStatus(MovieStatus.WATCHED);

        when(movieService.createMovie(any(MovieDto.class))).thenReturn(savedMovie);
        when(movieMapper.toDto(any(Movie.class))).thenReturn(inputDto);

        mockMvc.perform(post(API_MOVIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Valid Title"));
    }

    @Test
    void handleMovieServiceException_ShouldReturn400() throws Exception {
        MovieDto dto = createValidDto(); // Нужен валидный DTO, чтобы не сработал @Valid
        when(movieService.updateMovie(anyLong(), any(MovieDto.class)))
            .thenThrow(new MovieServiceException("Business Error"));

        mockMvc.perform(put(API_MOVIES + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_PATH).value("Business Logic Error"));
    }

    @Test
    void handleAlreadyExistsException_ShouldReturn409() throws Exception {
        MovieDto dto = createValidDto();
        when(movieService.createMovie(any(MovieDto.class)))
            .thenThrow(new AlreadyExistsException("Conflict Error"));

        mockMvc.perform(post(API_MOVIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath(ERROR_PATH).value("Conflict"));
    }

    @Test
    void handleRuntimeException_ShouldReturn404() throws Exception {
        doThrow(new RuntimeException("Not Found"))
            .when(movieService).deleteMovie(anyLong());

        mockMvc.perform(delete(API_MOVIES + "/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(ERROR_PATH).value("Resource Not Found"));
    }

    @Test
    void handleGlobalException_ShouldReturn500() throws Exception {
        doAnswer(invocation -> { throw new Exception("Server Error"); })
            .when(movieService).deleteMovie(anyLong());

        mockMvc.perform(delete(API_MOVIES + "/1"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath(ERROR_PATH).value("Internal Server Error"));
    }
}