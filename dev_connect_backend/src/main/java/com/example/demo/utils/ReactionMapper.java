package com.example.demo.utils;

import com.example.demo.dto.ReactionSummary;
import com.example.demo.entity.Reaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReactionMapper {

    public List<ReactionSummary> toReactionMap(List<Reaction> reactions) {
        if (reactions == null || reactions.isEmpty()) {
            return List.of();
        }

        return reactions.stream()
                .collect(Collectors.groupingBy(r -> r.getType().name()))
                .entrySet()
                .stream()
                .map(entry -> {
                    String type = entry.getKey();
                    List<Reaction> groupedReactions = entry.getValue();

                    int count = groupedReactions.size();

                    List<String> usernames = groupedReactions.stream()
                            .map(r -> r.getUser().getUsername())
                            .toList();

                    return new ReactionSummary(type, count, usernames);
                })
                .toList();
    }
}
