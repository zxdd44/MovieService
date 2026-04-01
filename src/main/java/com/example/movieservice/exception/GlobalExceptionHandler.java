package com.example.movieservice.exception;

import com.example.movieservice.service.MovieServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MovieServiceException.class)
    public ResponseEntity<ApiError> handleMovieServiceException(MovieServiceException ex) {
        LOG.error("Бизнес-ошибка (400 Bad Request): {}", ex.getMessage());
        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Business Logic Error",
            ex.getMessage()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();

        LOG.error("Ошибка валидации (400 Bad Request): {}", details);

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Переданные данные не прошли валидацию",
            details
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExistsException(AlreadyExistsException ex) {
        LOG.error("Конфликт данных (409 Conflict): {}", ex.getMessage());
        ApiError apiError = new ApiError(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage()
        );

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex) {
        LOG.error("Ресурс не найден (404 Not Found): {}", ex.getMessage());
        ApiError apiError = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage(),
            List.of()
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(Exception ex) {
        LOG.error("КРИТИЧЕСКАЯ ОШИБКА (500 Internal Server Error): ", ex);
        ApiError apiError = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Произошла непредвиденная ошибка: " + ex.getMessage()
        );
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
