package com.example.demo.dto;

import com.example.demo.enums.PostVisibility;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class PostRequest {
    @NotBlank
    public String title;
    public String content;
    public List<String> tags;
    @NotBlank public PostVisibility visibility = PostVisibility.PUBLIC;
}
