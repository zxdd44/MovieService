package com.example.movieservice.dto;

public class ReviewDTO {
    private Long id;
    private String username;
    private Long userId;
    private String content;
    private int rating;
    private String avatarUrl;

    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    public Long getId() {
        return id; }
    public void setId(Long id) {
        this.id = id; }
    public String getUsername() {
        return username; }
    public void setUsername(String username) {
        this.username = username; }
    public Long getUserId() {
        return userId; }
    public void setUserId(Long userId) {
        this.userId = userId; }
    public String getContent() {
        return content; }
    public void setContent(String content) {
        this.content = content; }
    public int getRating() {
        return rating; }
    public void setRating(int rating) {
        this.rating = rating; }
}
