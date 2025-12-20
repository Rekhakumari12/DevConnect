package com.example.demo.security;

import com.example.demo.model.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

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

    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated user");
        }

        UserPrincipal userDetails = (UserPrincipal) auth.getPrincipal();
        return userDetails.getId();
    }
}
