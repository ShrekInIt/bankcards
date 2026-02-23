package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CardCreateRequestDto(
        @NotNull Long ownerId,
        @NotBlank
        @Pattern(regexp = "\\d{16}", message = "PAN must be 16 digits")
        String pan,

        @NotNull LocalDate
        expiryDate
) { }
