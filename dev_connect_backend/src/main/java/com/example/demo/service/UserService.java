package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserProfileRequest;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.UUID;

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

    public UserProfileResponse register(UserProfileRequest req) {
        User user  = new User();
        user.setUsername(req.username());
        user.setBio(req.bio());
        user.setEmail(req.email());
        user.setSkills(req.skills());
        user.setPassword(passwordEncoder.encode(req.password()));
        userRepo.save(user);
        return new UserProfileResponse(user);
    }

    public UserProfileResponse getProfile(Principal principal) {
        String username = principal.getName();
        User user = userRepo.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("user not found"));
        return new UserProfileResponse(user);
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest req, UUID userId) {

        User user = userRepo.findById(userId);

        if (req.email() != null) user.setEmail(req.email());
        if (req.username() != null) user.setUsername(req.username());
        if (req.skills() != null) user.setSkills(req.skills());
        if (req.bio() != null) user.setBio(req.bio());

        userRepo.save(user);
        return new UserProfileResponse(user);
    }

    public String verify(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        if(authentication.isAuthenticated()) {
            return jwtUtil.generateToken(loginRequest.username());
        }else{
            throw new ResourceNotFoundException(("user not found!"));
        }
    }
}
