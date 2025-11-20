package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UserRepository userRepo;

    @Autowired
    PasswordEncoder passEncoder;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest)
    {
        User user = userRepo
                .findByUsername(loginRequest.username)
                .orElseThrow(() -> new RuntimeException(("User not found")));

        if(!passEncoder.matches(loginRequest.password, user.password)) {
            throw new RuntimeException(("Wrong password"));
        }

        String token = jwtUtil.generateToken(loginRequest.username);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
