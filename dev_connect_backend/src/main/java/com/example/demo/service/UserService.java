package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserProfile;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

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

    public String verify(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        if(authentication.isAuthenticated()) {
            return jwtUtil.generateToken(loginRequest.getUsername());
        }else{
            throw new RuntimeException(("User not found!"));
        }
    }
}
