package com.example.demo.service;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserProfile;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired // I need an object of this type here.
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthUtil authUtil;

    public UserProfile register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return new UserProfile(user);
    }

    public UserProfile getProfile(Principal principal) {
        String username = principal.getName();
        User user = userRepo.findByUsername(username)
                .orElseThrow(()-> new RuntimeException("User not found"));
        return new UserProfile(user);
    }

    public UserProfile updateProfile(UpdateProfileRequest req, Principal principal) {
        authUtil.verifyUserAccess(req.username);
        String username = principal.getName();
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.email != null) user.setEmail(req.email);
        if (req.username != null) user.setUsername(req.username);
        if (req.skills != null) user.setSkills(req.skills);
        if (req.bio != null) user.setBio(req.bio);

        userRepo.save(user);
        return new UserProfile(user);
    }
}
