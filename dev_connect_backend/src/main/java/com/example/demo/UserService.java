package com.example.demo;

import com.example.demo.dto.profile.PublicUserProfileResponse;
import com.example.demo.dto.profile.UpdateProfileRequest;
import com.example.demo.dto.profile.UserProfileRequest;
import com.example.demo.dto.profile.UserProfileResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

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

    public UserProfileResponse getProfileByUsernameForOwner(String username) {
        User user = getByUsername(username);
        return new UserProfileResponse(user);
    }

    public PublicUserProfileResponse getPublicProfileByUsername(String username) {
        User user = getByUsername(username);
        return new PublicUserProfileResponse(user);
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest req, UUID userId) {
        User user = getById(userId);

        // Only allow updating skills and bio; ignore username and email
        if (req.skills() != null) {
            user.setSkills(req.skills());
        }
        if (req.bio() != null) {
            user.setBio(req.bio());
        }

        userRepo.save(user);
        return new UserProfileResponse(user);
    }

    public User getById(UUID userId) {
        return userRepo.findById(userId)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));
    }

    public User getByUsername(String username) {
        return  userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
