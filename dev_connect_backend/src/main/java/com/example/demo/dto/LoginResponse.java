package com.example.demo.dto;

public class LoginResponse {
    final private String token;
    public LoginResponse(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }
}
