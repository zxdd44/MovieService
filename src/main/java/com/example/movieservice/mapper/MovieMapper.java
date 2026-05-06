package com.example.movieservice.mapper;

import com.example.movieservice.dto.MovieDto;
import com.example.movieservice.dto.ReviewDTO;
import com.example.movieservice.model.Genre;
import com.example.movieservice.model.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {
    public MovieDto toDto(Movie movie) {
        if (movie == null) {
            return null;
        }
        MovieDto dto = new MovieDto();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setYear(movie.getYear());
        dto.setImageUrl(movie.getImageUrl());
        if (movie.getStatus() != null) {
            dto.setStatus(movie.getStatus().getCode());
        }
        if (movie.getDirector() != null) {
            dto.setDirector(movie.getDirector().getName());
        }
        if (movie.getGenres() != null) {
            dto.setGenres(movie.getGenres().stream()
                .map(Genre::getName)
                .toList());
        }

        dto.setAverageRating(movie.getAverageRating());

        if (movie.getReviews() != null) {
            dto.setReviews(movie.getReviews().stream().map(review -> {
                ReviewDTO reviewDTO = new ReviewDTO();
                reviewDTO.setId(review.getId());
                reviewDTO.setContent(review.getContent());
                reviewDTO.setRating(review.getRating());
                if (review.getUser() != null) {
                    reviewDTO.setUserId(review.getUser().getId());
                    reviewDTO.setUsername(review.getUser().getUsername());
                    reviewDTO.setAvatarUrl(review.getUser().getAvatarUrl());
                }
                return reviewDTO;
            }).toList());
        }
        return dto;
    }
}
