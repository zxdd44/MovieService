package com.example.movieservice.dto;

import java.util.Objects;

public class MovieFilterKey {
    private final String director;
    private final String genre;
    private final int page;
    private final int size;
    private final String queryType;

    public MovieFilterKey(String director, String genre, int page, int size, String queryType) {
        this.director = director;
        this.genre = genre;
        this.page = page;
        this.size = size;
        this.queryType = queryType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MovieFilterKey that = (MovieFilterKey) o;
        return page == that.page &&
            size == that.size &&
            Objects.equals(director, that.director) &&
            Objects.equals(genre, that.genre) &&
            Objects.equals(queryType, that.queryType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(director, genre, page, size, queryType);
    }
}
