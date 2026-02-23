package com.example.movieservice.controller;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.mapper.MovieMapper;
import com.example.movieservice.service.MovieService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для работы с фильмами.
 */
@RestController
@RequestMapping("/api/movies")
public class MovieController {
  private final MovieService movieService;
  private final MovieMapper movieMapper;

  /**
   * Создаёт новый экземпляр контроллера фильмов.
   *
   * @param movieService сервис для работы с фильмами
   * @param movieMapper маппер для преобразования сущностей в DTO
   */
  public MovieController(MovieService movieService, MovieMapper movieMapper) {
    this.movieService = movieService;
    this.movieMapper = movieMapper;
  }

  /**
   * Возвращает фильм по его ID.
   *
   * @param id идентификатор фильма
   * @return DTO фильма
   */
  @GetMapping("/{id}") // PathVariable: /api/movies/1
  public MovieDto getById(@PathVariable Long id) {
    return movieMapper.toDto(movieService.getMovie(id));
  }

  /**
   * Возвращает фильмы по их автору.
   *
   * @param director автор фильма
   * @return DTO фильмов
   */
  @GetMapping("/filter") // RequestParam: /api/movies/filter?director=Nolan
  public List<MovieDto> getByDirector(@RequestParam String director) {
    return movieService.searchByDirector(director).stream()
        .map(movieMapper::toDto)
        .toList();
  }
}
