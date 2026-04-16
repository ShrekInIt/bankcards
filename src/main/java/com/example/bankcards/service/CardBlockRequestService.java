package com.example.bankcards.service;

/**
 * Сервис для создания заявок на блокировку карты.
 */
public interface CardBlockRequestService {
    /**
     * Создаёт заявку на блокировку карты.
     *
     * @param cardId идентификатор карты
     * @param userId идентификатор пользователя
     * @param reason причина блокировки
     * @return идентификатор созданной заявки
     */
    Long addRequest(Long cardId, Long userId, String reason);
}
