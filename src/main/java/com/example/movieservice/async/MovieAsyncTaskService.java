package com.example.movieservice.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MovieAsyncTaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovieAsyncTaskService.class);
    private final Map<String, TaskStatus> taskStatusMap = new ConcurrentHashMap<>();

    @Async
    public CompletableFuture<Void> processComplexBusinessLogic(String taskId) {
        try {
            LOGGER.info("Задача {} началась в потоке {}", taskId, Thread.currentThread().getName());
            taskStatusMap.put(taskId, TaskStatus.IN_PROGRESS);
            Thread.sleep(15000);
            taskStatusMap.put(taskId, TaskStatus.COMPLETED);
            LOGGER.info("Задача {} успешно завершена", taskId);
        } catch (InterruptedException e) {
            LOGGER.error("Задача {} была прервана", taskId);
            Thread.currentThread().interrupt();
            taskStatusMap.put(taskId, TaskStatus.ERROR);
        }
        return CompletableFuture.completedFuture(null);
    }

    public TaskStatus getTaskStatus(String taskId) {
        return taskStatusMap.getOrDefault(taskId, TaskStatus.NOT_FOUND);
    }
}
