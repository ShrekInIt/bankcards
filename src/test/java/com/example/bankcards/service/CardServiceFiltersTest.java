package com.example.bankcards.service;

import com.example.bankcards.dto.UserReadCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.repository.CardRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceFiltersTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    private Card sampleCard(long id, String last4, CardsStatus status, String balance, LocalDate expiry) {
        Card c = new Card();
        c.setId(id);
        c.setLast4(last4);
        c.setCardStatus(status);
        c.setBalance(new BigDecimal(balance));
        c.setExpiryDate(expiry);
        return c;
    }

    @Test
    void findAllCardsUserByStatusAndLast4_shouldReturnPagedDto_andPassCorrectPageable() {
        Long userId = 10L;
        String last4 = "1234";
        CardsStatus status = CardsStatus.active;
        int page = 0;
        int size = 2;

        Card c1 = sampleCard(1L, "1234", CardsStatus.active, "100.00", LocalDate.of(2027, 3, 31));
        long totalElements = 1L;

        PageRequest pr = PageRequest.of(page, size);
        Page<Card> cardsPage = new PageImpl<>(List.of(c1), pr, totalElements);

        when(cardRepository.findAllUserCardsByCardStatusAndCardLast4(eq(status), eq(last4), eq(userId), any(Pageable.class)))
                .thenReturn(cardsPage);

        Page<UserReadCardResponse> result =
                cardService.findAllCardsUserByStatusAndLast4(userId, last4, status, page, size);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(cardRepository, times(1))
                .findAllUserCardsByCardStatusAndCardLast4(eq(status), eq(last4), eq(userId), captor.capture());

        Pageable passed = captor.getValue();
        assertEquals(page, passed.getPageNumber());
        assertEquals(size, passed.getPageSize());

        assertEquals(totalElements, result.getTotalElements());
        assertEquals(1, result.getNumberOfElements());

        UserReadCardResponse dto = result.getContent().getFirst();
        assertEquals(1L, dto.getCardId());
        assertEquals(LocalDate.of(2027, 3, 31), dto.getExpiryDate());
        assertEquals(CardsStatus.active, dto.getCardStatus());
        assertEquals(new BigDecimal("100.00"), dto.getBalance());
        assertEquals("**** **** **** **** 1234", dto.getMaskedNumber());
    }

    @Test
    void findAllUserCardsByStatus_shouldReturnPagedDto_andPassCorrectPageable() {
        Long userId = 10L;
        CardsStatus status = CardsStatus.blocked;
        int page = 1;
        int size = 3;

        Card c1 = sampleCard(2L, "9999", CardsStatus.blocked, "0.00", LocalDate.of(2028, 12, 31));
        long totalElements = 4L;

        PageRequest pr = PageRequest.of(page, size);
        Page<Card> cardsPage = new PageImpl<>(List.of(c1), pr, totalElements);

        when(cardRepository.findAllUserCardsByCardStatus(eq(status), eq(userId), any(Pageable.class)))
                .thenReturn(cardsPage);

        Page<UserReadCardResponse> result =
                cardService.findAllUserCardsByStatus(userId, status, page, size);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(cardRepository, times(1))
                .findAllUserCardsByCardStatus(eq(status), eq(userId), captor.capture());

        Pageable passed = captor.getValue();
        assertEquals(page, passed.getPageNumber());
        assertEquals(size, passed.getPageSize());

        assertEquals(totalElements, result.getTotalElements());
        assertEquals(1, result.getNumberOfElements());

        UserReadCardResponse dto = result.getContent().getFirst();
        assertEquals(2L, dto.getCardId());
        assertEquals(CardsStatus.blocked, dto.getCardStatus());
        assertEquals("**** **** **** **** 9999", dto.getMaskedNumber());
    }

    @Test
    void findAllUserCardsByLast4_shouldReturnPagedDto_andPassCorrectPageable() {
        Long userId = 10L;
        String last4 = "7777";
        int page = 0;
        int size = 10;

        Card c1 = sampleCard(3L, "7777", CardsStatus.active, "50.00", LocalDate.of(2029, 1, 31));
        long totalElements = 1L;

        PageRequest pr = PageRequest.of(page, size);
        Page<Card> cardsPage = new PageImpl<>(List.of(c1), pr, totalElements);

        when(cardRepository.findAllUserCardsByCardLast4(eq(last4), eq(userId), any(Pageable.class)))
                .thenReturn(cardsPage);

        Page<UserReadCardResponse> result =
                cardService.findAllUserCardsByLast4(userId, last4, page, size);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(cardRepository, times(1))
                .findAllUserCardsByCardLast4(eq(last4), eq(userId), captor.capture());

        Pageable passed = captor.getValue();
        assertEquals(page, passed.getPageNumber());
        assertEquals(size, passed.getPageSize());

        assertEquals(totalElements, result.getTotalElements());
        assertEquals(1, result.getNumberOfElements());

        UserReadCardResponse dto = result.getContent().getFirst();
        assertEquals(3L, dto.getCardId());
        assertEquals("**** **** **** **** 7777", dto.getMaskedNumber());
        assertEquals(new BigDecimal("50.00"), dto.getBalance());
    }
}

