package com.example.demo.service;

import com.example.demo.CustomUserService;
import com.example.demo.entity.User;
import com.example.demo.model.UserPrincipal;
import com.example.demo.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserServiceTest {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private CustomUserService customUserService;

    @Test
    void loadUserByUsername_success() {
        User user = new User();
        user.setUsername("john");

        when(userRepo.findByUsername("john"))
                .thenReturn(Optional.of(user));

        UserDetails result = customUserService.loadUserByUsername("john");

        assertEquals("john", result.getUsername());
        assertEquals(UserPrincipal.class, result.getClass());
    }

    @Test
    void loadUserByUsername_userNotFound() {
        when(userRepo.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserService.loadUserByUsername("missing")
        );
    }
}
