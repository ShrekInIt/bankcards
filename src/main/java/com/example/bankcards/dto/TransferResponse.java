package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransferResponse(Long transactionId, Long fromCardId, Long toCardId, BigDecimal amount, OffsetDateTime createdAt, String comment) {
}
