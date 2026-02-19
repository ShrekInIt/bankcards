package com.example.bankcards.service;

import com.example.bankcards.dto.AdminCardDto;
import com.example.bankcards.dto.UserReadCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    @Transactional
    public Card findCardByIdForTransfer(Long id, Long userId) {
        return cardRepository.findByIdAndOwnerForUpdate(id, userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
    }

    @Transactional(readOnly = true)
    public Page<AdminCardDto> findAllCards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllByOrderByCreatedAtDesc(pageable);
        return cardsPage.map(Mapper::fromCardToAdminCardDto);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long cardId, Long userId) {
        Card card = cardRepository.findByIdAndCardOwner_Id(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));

        return card.getBalance();
    }

    @Transactional(readOnly = true)
    public Page<UserReadCardResponse> findAllCardsUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllByCardOwner_Id(userId, pageable);
        return cardsPage.map(Mapper::fromCardToUserReadCardResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserReadCardResponse> findAllCardsUserByStatusAndLast4(Long userId, String last4, CardsStatus cardStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardPage = cardRepository.findAllByCardOwner_IdAndCardStatusAndLast4(userId, cardStatus, last4, pageable);
        return cardPage.map(Mapper::fromCardToUserReadCardResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserReadCardResponse> findAllUserCardsByStatus(Long userId, CardsStatus cardStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllByCardOwner_IdAndCardStatus(userId, cardStatus, pageable);
        return cardsPage.map(Mapper::fromCardToUserReadCardResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserReadCardResponse> findAllUserCardsByLast4(Long userId, String last4, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllByCardOwner_IdAndLast4(userId, last4, pageable);
        return cardsPage.map(Mapper::fromCardToUserReadCardResponse);
    }
}
