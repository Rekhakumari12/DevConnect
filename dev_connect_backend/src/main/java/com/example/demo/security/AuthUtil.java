package com.example.demo.security;

import com.example.demo.model.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
public class AuthUtil {

    public void verifyUserAccess(UUID userId) {
        if (!isAuthenticated()) {
            throw new AccessDeniedException("You are not allowed to do this action!");
        }
    }

    private static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isAuthenticated()) {
            throw new RuntimeException("Unauthenticated user");
        }

        UserPrincipal userDetails = (UserPrincipal) auth.getPrincipal();
        return userDetails.getId();
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }
}
