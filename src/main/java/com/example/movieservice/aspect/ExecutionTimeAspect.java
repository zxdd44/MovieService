package com.example.movieservice.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("execution(* com.example.movieservice.service.MovieService.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - startTime;
        LOGGER.info("Метод [{}] выполнен за {} ms", joinPoint.getSignature().toShortString(), executionTime);

        return proceed;
    }
}
