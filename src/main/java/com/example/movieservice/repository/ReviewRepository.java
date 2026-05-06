package com.example.movieservice.repository;

import com.example.movieservice.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);
    List<Review> findByMovieId(Long movieId);
}
