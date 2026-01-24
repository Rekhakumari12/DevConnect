package com.example.demo.dto.reaction;

import java.util.List;

public record ReactionSummary(
         String type,
         int count,
         List<String> usernames
) {}

