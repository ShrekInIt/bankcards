package com.example.bankcards.service;

import com.example.bankcards.dto.UserReadCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    public Page<UserReadCardResponse> findAllCardsUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllUserCardsByUserId(userId, pageable);
        return cardsPage.map(Mapper::fromCardToUserReadCardResponse);
    }

    public Page<UserReadCardResponse> findAllCardsUserByStatusAndLast4(Long userId, String last4, CardsStatus cardStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardPage = cardRepository.findAllUserCardsByCardStatusAndCardLast4(cardStatus, last4, userId, pageable);
        return cardPage.map(Mapper::fromCardToUserReadCardResponse);
    }

    public Page<UserReadCardResponse> findAllUserCardsByStatus(Long userId, CardsStatus cardStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllUserCardsByCardStatus(cardStatus, userId, pageable);
        return cardsPage.map(Mapper::fromCardToUserReadCardResponse);
    }

    public Page<UserReadCardResponse> findAllUserCardsByLast4(Long userId, String last4, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllUserCardsByCardLast4(last4, userId, pageable);
        return cardsPage.map(Mapper::fromCardToUserReadCardResponse);
    }
}
