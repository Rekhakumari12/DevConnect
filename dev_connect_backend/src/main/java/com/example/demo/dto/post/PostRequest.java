package com.example.demo.dto.post;

import com.example.demo.enums.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must not exceed 120 characters")
        String title,
        
        @NotBlank(message = "Content is required")
        String content,
        
        @NotNull(message = "Tech stack is required")
        List<String> techStack,
        
        @NotNull(message = "Visibility is required")
        PostVisibility visibility
) {
    public PostRequest {
        if(visibility==null) visibility = PostVisibility.PUBLIC;
    }
};