package com.example.demo.service;

import com.example.demo.AuthService;
import com.example.demo.dto.login.LoginRequest;
import com.example.demo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testVerify_SuccessfulAuthentication() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user1", "pass123");
        Authentication mockAuth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(mockAuth.getName()).thenReturn("user1");
        when(jwtUtil.generateToken("user1")).thenReturn("mocked-jwt-token");

        // Act
        String token = authService.verify(loginRequest);

        // Assert
        assertNotNull(token);
        assertEquals("mocked-jwt-token", token);

        // Verify authenticationManager was called with correct username/password
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("user1", captor.getValue().getName());
        assertEquals("pass123", captor.getValue().getCredentials());

        // Verify JWT token generation
        verify(jwtUtil).generateToken("user1");
    }

    @Test
    void testVerify_InvalidCredentials_ShouldThrowException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user1", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.verify(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testVerify_WithEmptyCredentials_ShouldThrowAuthenticationException() {
        LoginRequest loginRequest = new LoginRequest("", "");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.verify(loginRequest)
        );

        assertEquals("Bad credentials", exception.getMessage());
    }


    @Test
    void testVerify_JwtGenerationCalledOnce() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user2", "pass456");
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(mockAuth.getName()).thenReturn("user2");
        when(jwtUtil.generateToken("user2")).thenReturn("jwt2");

        // Act
        String token = authService.verify(loginRequest);

        // Assert
        assertEquals("jwt2", token);
        verify(jwtUtil, times(1)).generateToken("user2");
    }
}
