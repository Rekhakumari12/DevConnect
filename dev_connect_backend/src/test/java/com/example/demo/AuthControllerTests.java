package com.example.demo;

import com.example.demo.controller.AuthController;
import com.example.demo.dto.login.LoginRequest;
import com.example.demo.dto.login.LoginResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)

public class AuthControllerTests {


        @InjectMocks
        private AuthController authController;

        @Mock
        private UserRepository userRepo;

        @Mock
        private PasswordEncoder passEncoder;

        @Mock
        private JwtUtil jwtUtil;

        private LoginRequest makeReq(String u, String p) {
            LoginRequest r = new LoginRequest();
            r.username = u;
            r.password = p;
            return r;
        }

        @Test
        void login_successful() {
            LoginRequest req = makeReq("alice", "secret");

            User mockUser = new User();
            mockUser.setUsername("alice");
            mockUser.setPassword("hashed");

            Mockito.when(userRepo.findByUsername("alice"))
                    .thenReturn(Optional.of(mockUser));

            Mockito.when(passEncoder.matches("secret", "hashed"))
                    .thenReturn(true);

            Mockito.when(jwtUtil.generateToken("alice"))
                    .thenReturn("tok123");

            ResponseEntity<LoginResponse> response = authController.login(req);

            Assertions.assertEquals(200, response.getStatusCode().value());
            Assertions.assertEquals("tok123", response.getBody().getToken());
        }

        @Test
        void login_userNotFound() {
            LoginRequest req = makeReq("ghost", "pw");

            Mockito.when(userRepo.findByUsername("ghost"))
                    .thenReturn(Optional.empty());

            RuntimeException ex = Assertions.assertThrows(
                    RuntimeException.class,
                    () -> authController.login(req)
            );

            Assertions.assertEquals("User not found", ex.getMessage());
        }

        @Test
        void login_wrongPassword() {
            LoginRequest req = makeReq("alice", "wrongpass");

            User mockUser = new User();
            mockUser.setUsername("alice");
            mockUser.setPassword("hashed");

            Mockito.when(userRepo.findByUsername("alice"))
                    .thenReturn(Optional.of(mockUser));

            Mockito.when(passEncoder.matches("wrongpass", "hashed"))
                    .thenReturn(false);

            RuntimeException ex = Assertions.assertThrows(
                    RuntimeException.class,
                    () -> authController.login(req)
            );

            Assertions.assertEquals("Wrong password", ex.getMessage());
        }

}

