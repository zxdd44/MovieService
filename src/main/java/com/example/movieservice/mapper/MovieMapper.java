package com.example.movieservice.mapper;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.model.Movie;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования сущностей Movie в DTO и обратно.
 */
@Component
public class MovieMapper {
  /**
   * Преобразует сущность Movie в объект MovieDto.
   *
   * @param movie сущность фильма, которую нужно преобразовать
   * @return DTO фильма или null, если входной объект равен null
   */
  public MovieDto toDto(Movie movie) {
    if (movie == null) {
      return null;
    }
    MovieDto dto = new MovieDto();
    dto.setTitle(movie.getTitle());
    dto.setDirector(movie.getDirector());
    dto.setYear(movie.getYear());
    dto.setStatus(movie.getStatus().getCode());
    return dto;
  }
}
