package com.example.movieservice.async;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MovieAsyncTaskServiceTest {
    private final MovieAsyncTaskService taskService = new MovieAsyncTaskService();

    @Test
    void testProcessComplexBusinessLogic_Success() {
        String taskId = "success-logic";
        taskService.processComplexBusinessLogic(taskId);
        Assertions.assertEquals(TaskStatus.COMPLETED, taskService.getTaskStatus(taskId));
    }

    @Test
    void testGetTaskStatus_ReturnsNotFound() {
        assertEquals(TaskStatus.NOT_FOUND, taskService.getTaskStatus("unknown"));
    }

    @Test
    void testProcessComplexBusinessLogic_HandlesInterruption() throws Exception {
        String testTaskId = "test-interrupt-id";
        Thread interruptThread = new Thread(() -> taskService.processComplexBusinessLogic(testTaskId));
        interruptThread.start();
        await().atMost(1, TimeUnit.SECONDS).until(interruptThread::isAlive);
        interruptThread.interrupt();
        interruptThread.join();
        assertEquals(TaskStatus.ERROR, taskService.getTaskStatus(testTaskId));
    }
}
