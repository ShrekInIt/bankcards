package com.example.bankcards.service;

import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponse;

/**
 * Сервис для выполнения переводов между картами.
 */
public interface TransactionService {

    /**
     * Создаёт транзакцию перевода между картами пользователя.
     *
     * @param transferRequest данные перевода
     * @param userId идентификатор пользователя
     * @return результат выполненного перевода
     */
    TransferResponse createTransaction(TransferRequest transferRequest, Long userId);
}
