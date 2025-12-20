package com.example.demo.dto;

import java.util.Map;

public class ReactionResponse {
    private Map<String, Integer> reactions;

    public Map<String, Integer> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, Integer> reactions) {
        this.reactions = reactions;
    }
}
