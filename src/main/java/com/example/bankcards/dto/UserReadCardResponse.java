package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardsStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserReadCardResponse(
        Long cardId,
        String maskedNumber,
        LocalDate expiryDate,
        CardsStatus cardStatus,
        BigDecimal balance,
        Long userId
) {
    public static UserReadCardResponse fromLast4(Long cardId,
                                                 String last4,
                                                 LocalDate expiryDate,
                                                 CardsStatus cardStatus,
                                                 BigDecimal balance,
                                                 Long userId) {
        return new UserReadCardResponse(
                cardId,
                "**** **** **** **** " + last4,
                expiryDate,
                cardStatus,
                balance,
                userId
        );
    }
}
