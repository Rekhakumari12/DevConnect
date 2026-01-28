package com.example.demo.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public class AuthUtil {

    public void verifyUserAccess(String username) {
        if (!isAuthenticated(username)) {
            throw new AccessDeniedException("You are not allowed to do this action!");
        }
    }


    public static boolean isAuthenticated(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.getName().equals(username)
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }
}
