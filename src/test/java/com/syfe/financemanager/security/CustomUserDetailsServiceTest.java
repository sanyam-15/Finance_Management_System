package com.syfe.financemanager.security;

import com.syfe.financemanager.model.User;
import com.syfe.financemanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_success() {
        User user = User.builder().id(1L).username("user@example.com").password("hash").fullName("John").phoneNumber("1").build();
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(user));

        assertEquals("user@example.com", customUserDetailsService.loadUserByUsername("user@example.com").getUsername());
    }

    @Test
    void loadUserByUsername_missing() {
        when(userRepository.findByUsername("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@example.com"));
    }
}
