package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardsStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AdminCardDto(
        Long id,
        String maskedPan,
        LocalDate expiryDate,
        OffsetDateTime createdAt,
        CardsStatus cardStatus,
        BigDecimal balance,
        Long ownerId,
        String ownerUsername
) {}
