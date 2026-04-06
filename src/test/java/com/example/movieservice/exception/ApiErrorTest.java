package com.example.movieservice.exception;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiErrorTest {

    @Test
    void testConstructorsAndGetters() {
        List<String> details = List.of("Field is required");

        ApiError error1 = new ApiError(400, "Bad Request", "Message");
        ApiError error2 = new ApiError(404, "Not Found", "Not Found Message", details);
        assertEquals(400, error1.getStatus());
        assertEquals("Bad Request", error1.getError());
        assertEquals("Message", error1.getMessage());
        assertNotNull(error1.getTimestamp());
        assertNull(error1.getDetails());
        assertEquals(404, error2.getStatus());
        assertEquals(details, error2.getDetails());
    }
}