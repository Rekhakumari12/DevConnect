package com.example.demo.utils;

import com.example.demo.entity.Reaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReactionMapper {
    public Map<String, Integer> toReactionMap(List<Reaction> reactions) {
        if (reactions == null || reactions.isEmpty()) {
            return Map.of();
        }
        return reactions.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getType().name(),
                        Collectors.summingInt(r -> 1)
                ));
    }
}
