package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PostResponse {
    public UUID id;
    public String title;
    public String content;
    public List<String> techStack;
    public String visibility;
    public String createdBy;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
