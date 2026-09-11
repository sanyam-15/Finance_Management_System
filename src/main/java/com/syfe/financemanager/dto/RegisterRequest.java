package com.syfe.financemanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Registration payload. Username must be an email address.
 */
@Data
public class RegisterRequest {
    @NotBlank
    @Email
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    @NotBlank
    private String phoneNumber;
}
