package com.example.bankcards.dto.transfer;

import java.math.BigDecimal;

public record TransferRequest(Long fromCardId, Long toCardId, BigDecimal amount, String comment) {
}
