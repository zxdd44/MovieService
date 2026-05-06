package com.example.movieservice.dto;

import java.util.List;
import java.util.Map;

public class UserProfileDto {
    private String username;
    private String status;
    private String avatarUrl;
    private Map<String, ChartData> statusChart;

    private List<MovieDto> watchedMovies;

    public static class ChartData {
        public long count;
        public double percentage;
        public ChartData(long count, double percentage) {
            this.count = count;
            this.percentage = percentage;
        }
    }

    public List<MovieDto> getWatchedMovies() {
        return watchedMovies;
    }
    public void setWatchedMovies(List<MovieDto> watchedMovies) {
        this.watchedMovies = watchedMovies;
    }
    public String getUsername() {
        return username; }
    public void setUsername(String username) {
        this.username = username; }
    public String getStatus() {
        return status; }
    public void setStatus(String status) {
        this.status = status; }
    public String getAvatarUrl() {
        return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl; }
    public Map<String, ChartData> getStatusChart() {
        return statusChart; }
    public void setStatusChart(Map<String, ChartData> statusChart) {
        this.statusChart = statusChart; }
}
