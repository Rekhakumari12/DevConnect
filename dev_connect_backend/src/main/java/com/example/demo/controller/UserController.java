package com.example.demo.controller;

import com.example.demo.dto.profile.UpdateProfileRequest;
import com.example.demo.dto.profile.UserProfileRequest;
import com.example.demo.dto.profile.UserProfileResponse;
import com.example.demo.model.UserPrincipal;
import com.example.demo.security.JwtUtil;
import com.example.demo.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody UserProfileRequest user,
                                                        HttpServletResponse response) {
        UserProfileResponse created = userService.register(user);

        // Auto-login: issue JWT cookie based on created username
        String token = jwtUtil.generateToken(created.username());
        ResponseCookie cookie = ResponseCookie.from("DEVCONNECT_JWT", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my-profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getProfileByUsername(principal.getName()));
    }

    @GetMapping("")
    public ResponseEntity<UserProfileResponse> getProfileByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.getProfileByUsername(username));
    }

    @PutMapping("/my-profile")
    public ResponseEntity<UserProfileResponse> updateMyProfile(@RequestBody UpdateProfileRequest req, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(userService.updateProfile(req, userPrincipal.getId()));
    }

}
