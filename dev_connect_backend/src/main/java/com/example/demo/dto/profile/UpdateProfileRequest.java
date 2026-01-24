package com.example.demo.dto.profile;


import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateProfileRequest (
    @NotBlank
    String email,
    @NotBlank
    String username,
    List<String> skills,
    String bio
){}
