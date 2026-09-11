package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.LoginRequest;
import com.syfe.financemanager.dto.RegisterRequest;
import com.syfe.financemanager.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_returnsCreated() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user@example.com");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setPhoneNumber("+1234567890");
        when(authService.register(request)).thenReturn(1L);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("User registered successfully", body.get("message"));
        assertEquals(1L, body.get("userId"));
    }

    @Test
    void login_returnsOkAndCreatesSession() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user@example.com");
        request.setPassword("password123");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(httpRequest.getSession(true)).thenReturn(session);

        ResponseEntity<?> response = authController.login(request, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Login successful", body.get("message"));
    }

    @Test
    void logout_invalidatesSession() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);

        ResponseEntity<?> response = authController.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Logout successful", body.get("message"));
    }

    @Test
    void logout_withoutSession_stillSucceeds() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        ResponseEntity<?> response = authController.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
