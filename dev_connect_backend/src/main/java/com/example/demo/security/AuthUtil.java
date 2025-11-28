package com.example.demo.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class AuthUtil {
    public void verifyUserAccess(String requestedUsername) {
        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        String tokenUsername = principal.getName();

        if(!tokenUsername.equals(requestedUsername)) {
            throw new AccessDeniedException("Post not found or not owned by you");
        }
    }
}
