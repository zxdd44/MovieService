package com.example.movieservice.dto;

import java.util.List;

public class MovieDto {
    private String title;
    private String director;
    private int year;
    private int status;
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
