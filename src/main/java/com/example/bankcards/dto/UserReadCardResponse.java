package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardsStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserReadCardResponse {
    private Long cardId;
    private String maskedNumber;
    private LocalDate expiryDate;
    private CardsStatus cardStatus;
    private BigDecimal balance;
    private Long userId;

    public UserReadCardResponse(Long cardId, String last4, LocalDate expiryDate, CardsStatus cardStatus, BigDecimal balance) {
        this.cardId = cardId;
        this.expiryDate = expiryDate;
        this.cardStatus = cardStatus;
        this.balance = balance;

        maskedNumber = "**** **** **** **** " + last4;
    }
}
