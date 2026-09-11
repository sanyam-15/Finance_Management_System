package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.RegisterRequest;
import com.syfe.financemanager.exception.ConflictException;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setUsername("user@example.com");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setPhoneNumber("+1234567890");
    }

    @Test
    void register_success() {
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        Long userId = authService.register(request);

        assertEquals(1L, userId);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed", captor.getValue().getPassword());
        assertEquals("John Doe", captor.getValue().getFullName());
        verify(categoryService).initializeDefaultCategories();
    }

    @Test
    void register_duplicateUsername_throwsConflict() {
        when(userRepository.findByUsername(request.getUsername()))
                .thenReturn(Optional.of(User.builder().id(1L).username(request.getUsername()).build()));

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
