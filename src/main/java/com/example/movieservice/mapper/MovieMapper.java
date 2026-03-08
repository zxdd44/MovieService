package com.example.movieservice.mapper;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.model.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {
    public MovieDto toDto(Movie movie) {
        if (movie == null) {
            return null;
        }
        MovieDto dto = new MovieDto();
        dto.setTitle(movie.getTitle());
        dto.setYear(movie.getYear());
        if (movie.getStatus() != null) {
            dto.setStatus(movie.getStatus().getCode());
        }
        if (movie.getDirector() != null) {
            dto.setDirector(movie.getDirector().getName());
        }
        return dto;
    }
}
