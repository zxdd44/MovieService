package com.example.movieservice.dto;

/**
 * DTO для передачи информации пользователю.
 */
public class MovieDto {
  private String title;
  private String director;
  private int year;
  private int status; // Тут статус передаем просто числом

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDirector() {
    return director;
  }

  public void setDirector(String director) {
    this.director = director;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}
