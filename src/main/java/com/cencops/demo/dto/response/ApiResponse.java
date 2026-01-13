package com.cencops.demo.dto.response;

import java.time.Instant;

public class ApiResponse {

    private String status;
    private String message;
    private Instant timestamp;

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
}
