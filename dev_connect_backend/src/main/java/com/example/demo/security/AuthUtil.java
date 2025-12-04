package com.example.demo.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class AuthUtil {
    public boolean isSameUser(String requestedUsername) {
        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        String tokenUsername = principal.getName();
        return tokenUsername.equals(requestedUsername);
    }

    public void verifyUserAccess(String requestedUsername) {
        if (!isSameUser(requestedUsername)) {
            throw new AccessDeniedException("You are not allowed to do this action!");
        }
    }
}
