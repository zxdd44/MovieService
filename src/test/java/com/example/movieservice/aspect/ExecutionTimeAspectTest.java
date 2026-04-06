package com.example.movieservice.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ExecutionTimeAspectTest {

    private final ExecutionTimeAspect aspect = new ExecutionTimeAspect();

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Test
    void logExecutionTime_ShouldProceedAndReturnResult() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("MovieService.getMovie()");
        when(joinPoint.proceed()).thenReturn("Method Result");
        Object result = aspect.logExecutionTime(joinPoint);
        assertEquals("Method Result", result);
        verify(joinPoint, times(1)).proceed();
    }
}