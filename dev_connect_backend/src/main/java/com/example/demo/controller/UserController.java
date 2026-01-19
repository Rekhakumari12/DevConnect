package com.example.demo.controller;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserProfileRequest;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.entity.User;
import com.example.demo.model.UserPrincipal;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody UserProfileRequest user) {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal));
    }

    @GetMapping("")
    public ResponseEntity<UserProfileResponse> getProfileByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.getProfileByUsername(username));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(@RequestBody UpdateProfileRequest req, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(userService.updateProfile(req, userPrincipal.getId()));
    }

}
