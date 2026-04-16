package com.example.bankcards.dto.user;

import com.example.bankcards.entity.enums.UsersStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "User status is required")
        UsersStatus userStatus,

        @NotNull(message = "Active flag is required")
        Boolean isActive
) {}
