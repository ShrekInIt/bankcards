package com.example.bankcards.service;

import com.example.bankcards.dto.card.AdminCardDto;
import com.example.bankcards.dto.card.CardCreateRequestDto;
import com.example.bankcards.dto.user.UserReadCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardsStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

/**
 * Сервис для работы с банковскими картами.
 * Используется для получения, создания, изменения статуса и просмотра карт.
 */
public interface CardService {

    /**
     * Возвращает карту для выполнения перевода с проверкой принадлежности пользователю.
     *
     * @param id идентификатор карты
     * @param userId идентификатор пользователя
     * @return найденная карта
     */
    Card findCardByIdForTransfer(Long id, Long userId);

    /**
     * Возвращает список всех карт с постраничным выводом для администратора.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @return страница с картами
     */
    Page<AdminCardDto> findAllCards(int page, int size);

    /**
     * Создаёт новую карту.
     *
     * @param request данные для создания карты
     * @return созданная карта
     */
    AdminCardDto createCard(CardCreateRequestDto request);

    /**
     * Возвращает карту по идентификатору.
     *
     * @param id идентификатор карты
     * @return данные карты
     */
    AdminCardDto getCardById(Long id);

    /**
     * Активирует карту.
     *
     * @param id идентификатор карты
     * @param userId идентификатор пользователя
     * @return обновлённые данные карты
     */
    AdminCardDto activateCard(Long id, Long userId);

    /**
     * Блокирует карту.
     *
     * @param id идентификатор карты
     * @param userId идентификатор пользователя
     * @return обновлённые данные карты
     */
    AdminCardDto blockCard(Long id, Long userId);

    /**
     * Удаляет карту по идентификатору.
     *
     * @param id идентификатор карты
     */
    void deleteCard(Long id);

    /**
     * Возвращает текущий баланс карты.
     *
     * @param cardId идентификатор карты
     * @param userId идентификатор пользователя
     * @return баланс карты
     */
    BigDecimal getBalance(Long cardId, Long userId);

    /**
     * Возвращает все карты пользователя с постраничным выводом.
     *
     * @param userId идентификатор пользователя
     * @param page номер страницы
     * @param size размер страницы
     * @return страница с картами пользователя
     */
    Page<UserReadCardResponse> findAllCardsUser(Long userId, int page, int size);

    /**
     * Возвращает карты пользователя с фильтрацией по статусу и последним цифрам номера.
     *
     * @param userId идентификатор пользователя
     * @param last4 последние 4 цифры номера карты
     * @param cardStatus статус карты
     * @param page номер страницы
     * @param size размер страницы
     * @return страница с отфильтрованными картами
     */
    Page<UserReadCardResponse> findAllCardsUserByStatusAndLast4(Long userId, String last4, CardsStatus cardStatus, int page, int size);

    /**
     * Возвращает карты пользователя по статусу.
     *
     * @param userId идентификатор пользователя
     * @param cardStatus статус карты
     * @param page номер страницы
     * @param size размер страницы
     * @return страница с картами
     */
    Page<UserReadCardResponse> findAllUserCardsByStatus(Long userId, CardsStatus cardStatus, int page, int size);

    /**
     * Возвращает карты пользователя по последним 4 цифрам номера.
     *
     * @param userId идентификатор пользователя
     * @param last4 последние 4 цифры номера карты
     * @param page номер страницы
     * @param size размер страницы
     * @return страница с картами
     */
    Page<UserReadCardResponse> findAllUserCardsByLast4(Long userId, String last4, int page, int size);

}
