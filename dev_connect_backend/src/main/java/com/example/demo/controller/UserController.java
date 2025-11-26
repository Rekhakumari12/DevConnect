package com.example.demo.controller;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserProfile;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepo;

    @Autowired // I need an object of this type here.
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> findAll() {
        return userRepo.findAll();
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody User user) {
            user.password = passwordEncoder.encode(user.password);
            return userRepo.save(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMyProfile(Principal principal) {
        String username = principal.getName();
        User user = userRepo.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));
        return ResponseEntity.ok(new UserProfile(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfile> updateMyProfile(@RequestBody UpdateProfileRequest req, Principal principal) {
        String username = principal.getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (req.email != null) user.email = req.email;
        if (req.username != null) user.username = req.username;
        if (req.skills != null) user.skills = req.skills;
        if (req.bio != null) user.bio = req.bio;

        userRepo.save(user);

        return ResponseEntity.ok(new UserProfile(user));
    }

}
