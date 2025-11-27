package com.example.demo.controller;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserProfile;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserProfile> register(@Valid @RequestBody User user) {
            return ResponseEntity.ok(userService.register(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfile> updateMyProfile(@RequestBody UpdateProfileRequest req, Principal principal) {
        return ResponseEntity.ok(userService.updateProfile(req, principal));
    }

}
