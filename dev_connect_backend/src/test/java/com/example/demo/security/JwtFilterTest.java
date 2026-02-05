package com.example.demo.security;

import com.example.demo.CustomUserService;
import com.example.demo.entity.User;
import com.example.demo.model.UserPrincipal;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ApplicationContext context;

    @Mock
    private CustomUserService customUserService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidCookie_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/users/my-profile");
        
        Cookie jwtCookie = new Cookie("DEVCONNECT_JWT", "valid-token");
        request.setCookies(jwtCookie);
        
        User user = new User();
        user.setUsername("testuser");
        UserPrincipal userPrincipal = new UserPrincipal(user);
        
        when(jwtUtil.extractUsername("valid-token")).thenReturn("testuser");
        when(context.getBean(CustomUserService.class)).thenReturn(customUserService);
        when(customUserService.loadUserByUsername("testuser")).thenReturn(userPrincipal);
        when(jwtUtil.IsTokenValid("valid-token", userPrincipal)).thenReturn(true);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("testuser");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidAuthorizationHeader_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/users/my-profile");
        request.addHeader("Authorization", "Bearer valid-token");
        
        User user = new User();
        user.setUsername("testuser");
        UserPrincipal userPrincipal = new UserPrincipal(user);
        
        when(jwtUtil.extractUsername("valid-token")).thenReturn("testuser");
        when(context.getBean(CustomUserService.class)).thenReturn(customUserService);
        when(customUserService.loadUserByUsername("testuser")).thenReturn(userPrincipal);
        when(jwtUtil.IsTokenValid("valid-token", userPrincipal)).thenReturn(true);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoCookie_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/users/my-profile");

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/users/my-profile");
        
        Cookie jwtCookie = new Cookie("DEVCONNECT_JWT", "invalid-token");
        request.setCookies(jwtCookie);
        
        User user = new User();
        user.setUsername("testuser");
        UserPrincipal userPrincipal = new UserPrincipal(user);
        
        when(jwtUtil.extractUsername("invalid-token")).thenReturn("testuser");
        when(context.getBean(CustomUserService.class)).thenReturn(customUserService);
        when(customUserService.loadUserByUsername("testuser")).thenReturn(userPrincipal);
        when(jwtUtil.IsTokenValid("invalid-token", userPrincipal)).thenReturn(false);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_PublicEndpoints_ShouldSkipFilter() throws ServletException, IOException {
        // Arrange
        String[] publicPaths = {
            "/auth/login",
            "/auth/logout",
            "/api/users/register",
            "/api/posts/public",
            "/api/search"
        };
        
        for (String path : publicPaths) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setServletPath(path);
            
            SecurityContextHolder.clearContext();

            // Act
            jwtFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
            verify(jwtUtil, never()).extractUsername(anyString());
        }
    }

    @Test
    void doFilterInternal_CookiePrefersOverHeader_WhenBothPresent() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/users/my-profile");
        
        Cookie jwtCookie = new Cookie("DEVCONNECT_JWT", "cookie-token");
        request.setCookies(jwtCookie);
        request.addHeader("Authorization", "Bearer header-token");
        
        User user = new User();
        user.setUsername("testuser");
        UserPrincipal userPrincipal = new UserPrincipal(user);
        
        when(jwtUtil.extractUsername("cookie-token")).thenReturn("testuser");
        when(context.getBean(CustomUserService.class)).thenReturn(customUserService);
        when(customUserService.loadUserByUsername("testuser")).thenReturn(userPrincipal);
        when(jwtUtil.IsTokenValid("cookie-token", userPrincipal)).thenReturn(true);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil).extractUsername("cookie-token");
        verify(jwtUtil, never()).extractUsername("header-token");
    }
}
