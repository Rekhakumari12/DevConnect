package com.example.demo.dto;

import com.example.demo.enums.PostVisibility;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PostRequest(
        @NotBlank
        String title,
        String content,
        List<String> techStack,
        @NotBlank PostVisibility visibility
) {
    public PostRequest {
        if(visibility==null) visibility = PostVisibility.PUBLIC;
    }
};