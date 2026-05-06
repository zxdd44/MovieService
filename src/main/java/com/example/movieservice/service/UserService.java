package com.example.movieservice.service;

import com.example.movieservice.dto.UserProfileDto;
import com.example.movieservice.model.User;
import com.example.movieservice.model.UserMovieProgress;
import com.example.movieservice.repository.UserMovieProgressRepository;
import com.example.movieservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMovieProgressRepository progressRepository;

    public UserService(UserRepository userRepository, UserMovieProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
    }

    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getUsername());
        dto.setStatus(user.getStatus());
        dto.setAvatarUrl(user.getAvatarUrl());
        List<UserMovieProgress> progressList = progressRepository.findByUserId(userId);
        long watched = 0, deferred = 0, abandoned = 0;
        for (UserMovieProgress p : progressList) {
            if (p.getStatus() != null) {
                switch (p.getStatus().name()) {
                    case "WATCHED": watched++; break;
                    case "DEFERRED": deferred++; break;
                    case "ABANDONED": abandoned++; break;
                }
            }
        }

        long total = watched + deferred + abandoned;
        Map<String, UserProfileDto.ChartData> chart = new HashMap<>();
        chart.put("WATCHED", new UserProfileDto.ChartData(watched, total > 0 ? (watched * 100.0 / total) : 0));
        chart.put("DEFERRED", new UserProfileDto.ChartData(deferred, total > 0 ? (deferred * 100.0 / total) : 0));
        chart.put("ABANDONED", new UserProfileDto.ChartData(abandoned, total > 0 ? (abandoned * 100.0 / total) : 0));
        dto.setStatusChart(chart);
        return dto;
    }

    @Transactional
    public UserProfileDto updateUserProfile(Long userId, UserProfileDto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        userRepository.save(user);
        return getUserProfile(userId);
    }
}