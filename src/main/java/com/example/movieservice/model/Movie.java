package com.example.movieservice.model;

/**
 * Модель из чего должен состоять фильм + геттеры.
 */
public class Movie {
  private final Long id;
  private final String title;
  private final String director;
  private final int year;
  private MovieStatus status;

  /**
   * Получает данные фильм.
   *
   * @param id идентификатор фильма
   * @param title название фильма
   * @param director автор фильма
   * @param year год выпуска фильма
   * @param status состояние фильма для пользователя
   */
  public Movie(Long id, String title, String director, int year, MovieStatus status) {
    this.id = id;
    this.title = title;
    this.director = director;
    this.year = year;
    this.status = status;
  }

  // Геттеры
  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDirector() {
    return director;
  }

  public int getYear() {
    return year;
  }

  public MovieStatus getStatus() {
    return status;
  }
}
