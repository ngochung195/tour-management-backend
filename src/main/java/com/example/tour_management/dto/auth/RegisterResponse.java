package com.example.tour_management.dto.auth;

public class RegisterResponse {
    private String message;

    public RegisterResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
