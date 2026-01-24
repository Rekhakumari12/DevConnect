package com.example.demo.controller;

import com.example.demo.dto.login.LoginRequest;
import com.example.demo.dto.login.LoginResponse;
import com.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.verify(loginRequest);
        return ResponseEntity.ok(new LoginResponse(token));
    }

}
