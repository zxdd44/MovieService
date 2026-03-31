package com.example.movieservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "DTO для передачи данных о фильме")
public class MovieDto {
    @Schema(description = "Название фильма", example = "Интерстеллар")
    @NotBlank(message = "Название фильма не может быть пустым")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String title;

    @Schema(description = "Имя режиссера", example = "Кристофер Нолан")
    @NotBlank(message = "Имя режиссера не может быть пустым")
    private String director;

    @Schema(description = "Год выпуска", example = "2014")
    @Min(value = 1888, message = "Год выпуска не может быть раньше 1888")
    private int year;

    @Schema(description = "Код статуса фильма", example = "1")
    @NotNull(message = "Статус должен быть указан")
    private int status;

    @Schema(description = "Список жанров", example = "[\"Sci-Fi\", \"Drama\"]")
    private List<String> genres;

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
    public List<String> getGenres() {
        return genres; }
    public void setGenres(List<String> genres) {
        this.genres = genres; }
}
