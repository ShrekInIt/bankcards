package com.example.bankcards.dto;

import java.math.BigDecimal;

public record TransferRequest(Long fromCardId, Long toCardId, BigDecimal amount, String comment) {
}
