package com.example.demo1.model;


public class AuthResponse {
    private boolean success;
    private String token;
    private String role;
    private User user;
    private String message;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(boolean success, String token, String role, User user, String message) {
        this.success = success;
        this.token = token;
        this.role = role;
        this.user = user;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}