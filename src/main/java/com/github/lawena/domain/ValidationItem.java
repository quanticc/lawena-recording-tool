package com.github.lawena.domain;

import java.time.LocalDateTime;

public class ValidationItem {

    private final LocalDateTime time;
    private final String type;
    private final String message;

    public ValidationItem(String type, String message) {
        this.time = LocalDateTime.now();
        this.type = type;
        this.message = message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ValidationItem [" +
                "time=" + time +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ']';
    }
}
