package com.example.movieservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MovieFilterKeyTest {

    @Test
    void testEqualsAndHashCode() {
        MovieFilterKey key1 = new MovieFilterKey("Nolan", "Action", 0, 10, "native");
        MovieFilterKey key2 = new MovieFilterKey("Nolan", "Action", 0, 10, "native");
        MovieFilterKey key3 = new MovieFilterKey("Tarantino", "Drama", 1, 20, "jpql");
        assertEquals(key1, key1);
        assertEquals(key1, key2);
        assertNotEquals(key1, key3);
        assertNotNull(key1);
        assertNotEquals(key1, new Object());
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1.hashCode(), key3.hashCode());
    }
}