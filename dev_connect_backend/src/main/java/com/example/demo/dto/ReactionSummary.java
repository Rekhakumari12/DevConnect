package com.example.demo.dto;

import java.util.List;

public record ReactionSummary(
         String type,
         int count,
         List<String> usernames
) {}

