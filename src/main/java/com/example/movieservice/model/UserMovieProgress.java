package com.example.movieservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_movie_progress")
public class UserMovieProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Enumerated(EnumType.ORDINAL)
    private MovieStatus status;

    public Long getId() {
        return id; }
    public void setId(Long id) {
        this.id = id; }
    public User getUser() {
        return user; }
    public void setUser(User user) {
        this.user = user; }
    public Movie getMovie() {
        return movie; }
    public void setMovie(Movie movie) {
        this.movie = movie; }
    public MovieStatus getStatus() {
        return status; }
    public void setStatus(MovieStatus status) {
        this.status = status; }
}