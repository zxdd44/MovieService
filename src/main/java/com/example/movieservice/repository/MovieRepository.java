package com.example.movieservice.repository;

import com.example.movieservice.model.Movie;
import com.example.movieservice.model.MovieStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для хранения и получения фильмов.
 */
@Repository
public class MovieRepository {
  private final List<Movie> database = new ArrayList<>();

  /**
   * Создаёт репозиторий и инициализирует его тестовыми данными.
   */
  public MovieRepository() {
    database.add(new Movie(1L, "Interstellar", "Christopher Nolan", 2014, MovieStatus.WATCHED));
    database.add(new Movie(2L, "The Green Mile", "Frank Darabont", 1999, MovieStatus.NOT_WATCHED));
  }

  /**
   * Возвращает список всех фильмов, хранящихся в репозитории.
   *
   * @return список фильмов
   */
  public List<Movie> findAll() {
    return database;
  }

  /**
   * Ищет фильм по его идентификатору.
   *
   * @param id идентификатор фильма
   * @return найденный фильм или null, если фильм не найден
   */
  public Movie findById(Long id) {
    return database.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
  }
}
