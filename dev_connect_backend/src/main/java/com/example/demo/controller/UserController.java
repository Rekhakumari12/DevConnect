package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    public User register(@Valid @RequestBody User user) {
            user.password = passwordEncoder.encode(user.password);
            return userRepo.save(user);
    }
}
