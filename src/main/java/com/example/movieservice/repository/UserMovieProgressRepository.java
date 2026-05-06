package com.example.movieservice.repository;

import com.example.movieservice.model.UserMovieProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMovieProgressRepository extends JpaRepository<UserMovieProgress, Long> {
    List<UserMovieProgress> findByUserId(Long userId);
    Optional<UserMovieProgress> findByUserIdAndMovieId(Long userId, Long movieId);
}