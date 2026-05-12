package com.example.movieservice.controller;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.exception.AlreadyExistsException;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.model.Movie;
import com.example.movieservice.repository.MovieRepository;
import com.example.movieservice.repository.UserMovieProgressRepository;
import com.example.movieservice.repository.UserRepository;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    @MockBean
    private MovieMapper movieMapper;

    @MockBean
    private UserMovieProgressRepository progressRepository;

    @MockBean
    private MovieRepository movieRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    void searchMovies_ShouldReturnPage() throws Exception {
        when(movieService.searchComplex(any(), any(), any(), any(Pageable.class), anyBoolean()))
            .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/movies/search")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createMovie_ShouldReturnSavedMovie() throws Exception {
        MovieDto dto = new MovieDto();
        dto.setTitle("Inception");
        when(movieService.createMovie(any(MovieDto.class))).thenReturn(new Movie());
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void handleMovieServiceException_ShouldReturn400() throws Exception {
        MovieDto dto = new MovieDto();
        dto.setTitle("Test Movie");
        when(movieService.createMovie(any(MovieDto.class)))
            .thenThrow(new MovieServiceException("Business Logic Error"));
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest()) // Ожидаем 400
            .andExpect(jsonPath("$.message").value("Business Logic Error"));
    }

    @Test
    void handleAlreadyExistsException_ShouldReturn409() throws Exception {
        when(movieService.createMovie(any())).thenThrow(new AlreadyExistsException("Conflict"));
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new MovieDto())))
            .andExpect(status().isConflict());
    }

    @Test
    void handleRuntimeException_ShouldReturn404() throws Exception {
        doThrow(new RuntimeException("Not Found")).when(movieService).deleteMovie(999L);
        mockMvc.perform(delete("/api/movies/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void handleGlobalException_ShouldReturn500() throws Exception {
        doThrow(new RuntimeException("Unexpected Server Error"))
            .when(movieService).deleteMovie(anyLong());
        mockMvc.perform(delete("/api/movies/1"))
            .andExpect(status().isInternalServerError());
    }
}