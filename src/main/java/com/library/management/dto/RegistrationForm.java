package com.library.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationForm {
    @NotBlank(message="Username is required")
    @Size(min=3, max=50)
    private String username;

    @NotBlank(message="Email is required")
    @Email
    private String email;

    @NotBlank(message="Password is required")
    @Size(min=6, message="Password must be at least 6 characters")
    private String password;

    @NotBlank(message="Please confirm password")
    private String confirmPassword;
}
