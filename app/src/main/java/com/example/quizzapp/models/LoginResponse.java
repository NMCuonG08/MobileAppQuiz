package com.example.quizzapp.models;

class LoginResponse {
    private boolean success;
    private String message;
    private String sessionId;
    private UserData user;

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getSessionId() { return sessionId; }
    public UserData getUser() { return user; }
}

