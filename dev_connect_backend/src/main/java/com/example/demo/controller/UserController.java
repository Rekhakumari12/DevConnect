package com.example.demo.controller;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserProfile;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
    public ResponseEntity<UserProfile> register(@Valid @RequestBody User user) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepo.save(user);
            return ResponseEntity.ok().body(new UserProfile(user));
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

        if (req.email != null) user.setEmail(req.email);
        if (req.username != null) user.setUsername(req.username);
        if (req.skills != null) user.setSkills(req.skills);
        if (req.bio != null) user.setBio(req.bio);

        userRepo.save(user);

        return ResponseEntity.ok(new UserProfile(user));
    }

}
