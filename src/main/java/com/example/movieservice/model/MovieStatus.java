package com.example.movieservice.model;

public enum MovieStatus {
    NOT_WATCHED(0), WATCHED(1), DEFERRED(2), ABANDONED(3);

    private final int code;

    MovieStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
