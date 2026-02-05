package com.example.demo.security;

import com.example.demo.model.UserPrincipal;
import com.example.demo.repository.UserRepository;
import com.example.demo.CustomUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = null;
        String usernameFromToken = null;
        // Skip public endpoints
        String path = request.getServletPath();
        if (path.startsWith("/auth/login")
                || path.startsWith("/auth/logout")
                || path.startsWith("/api/users/register")
                || path.startsWith("/api/posts/public")
                || path.startsWith("/api/search")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = resolveToken(request);
        if (token != null) {
            usernameFromToken = jwtUtil.extractUsername(token);
        }

        if (usernameFromToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // userDetails from database
            UserPrincipal userPrincipal =
                    (UserPrincipal) context.getBean(CustomUserService.class).loadUserByUsername(usernameFromToken);

            if(jwtUtil.IsTokenValid(token, userPrincipal)) { // username from request
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, Collections.emptyList());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // Prefer cookie-based token for session semantics
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("DEVCONNECT_JWT".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // (Optional) Fallback to Authorization header for compatibility
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
