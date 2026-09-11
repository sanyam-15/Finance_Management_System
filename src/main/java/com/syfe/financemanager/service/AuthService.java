package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.RegisterRequest;
import com.syfe.financemanager.exception.ConflictException;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for user registration.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;

    /**
     * Registers a new user with a BCrypt-hashed password.
     *
     * @param request registration payload
     * @return the created user id
     */
    @Transactional
    public Long register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);

        categoryService.initializeDefaultCategories();

        return savedUser.getId();
    }
}
