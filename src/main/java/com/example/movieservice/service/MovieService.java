package com.example.movieservice.service;

import com.example.movieservice.model.Movie;
import com.example.movieservice.repository.MovieRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Сервисный слой для работы с фильмами.
 */
@Service
public class MovieService {
  private final MovieRepository repository;

  /**
   * Создаёт экземпляр сервиса и инициализирует его репозиторием фильмов.
   *
   * @param repository репозиторий, используемый для доступа к данным о фильмах
   */
  public MovieService(MovieRepository repository) {
    this.repository = repository;
  }

  /**
   * Возвращает фильм по его идентификатору.
   *
   * @param id идентификатор фильма
   * @return найденный фильм или null, если фильм не найден
   */
  public Movie getMovie(Long id) {
    return repository.findById(id);
  }

  /**
   * Выполняет поиск фильмов по имени режиссёра.
   *
   * @param director автор
   * @return список фильмов, снятых указанным автором
   */
  public List<Movie> searchByDirector(String director) {
    return repository.findAll().stream()
        .filter(m -> m.getDirector().equalsIgnoreCase(director))
        .toList();
  }
}
