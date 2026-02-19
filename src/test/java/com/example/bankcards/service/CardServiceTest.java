package com.example.bankcards.service;

import com.example.bankcards.dto.UserReadCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    CardRepository cardRepository;
    @InjectMocks
    CardService cardService;

    @BeforeEach
    void setup() {

    }

    @Test
    void findAllCardsUser_shouldReturnPagedDto_andPassCorrectPageable() {
        // arrange
        Long userId = 10L;
        int page = 0;
        int size = 2;

        Card c1 = new Card();
        c1.setId(1L);
        c1.setLast4("1234");
        c1.setExpiryDate(LocalDate.of(2027, 3, 31));
        c1.setCardStatus(CardsStatus.active);
        c1.setBalance(new BigDecimal("100.00"));

        Card c2 = new Card();
        c2.setId(2L);
        c2.setLast4("9876");
        c2.setExpiryDate(LocalDate.of(2028, 12, 31));
        c2.setCardStatus(CardsStatus.blocked);
        c2.setBalance(BigDecimal.ZERO);

        long totalElements = 5L;
        PageRequest pr = PageRequest.of(page, size);
        Page<Card> cardsPage = new PageImpl<>(List.of(c1, c2), pr, totalElements);

        when(cardRepository.findAllByCardOwner_Id(eq(userId), any(Pageable.class)))
                .thenReturn(cardsPage);

        // act
        Page<UserReadCardResponse> result = cardService.findAllCardsUser(userId, page, size);

        // assert: repository called with correct pageable
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(cardRepository, times(1))
                .findAllByCardOwner_Id(eq(userId), pageableCaptor.capture());

        Pageable passed = pageableCaptor.getValue();
        assertEquals(page, passed.getPageNumber());
        assertEquals(size, passed.getPageSize());

        // assert: page metadata preserved
        assertEquals(totalElements, result.getTotalElements());
        assertEquals((int) Math.ceil((double) totalElements / size), result.getTotalPages());
        assertEquals(2, result.getNumberOfElements());

        // assert: DTO mapping
        List<UserReadCardResponse> content = result.getContent();
        assertEquals(1L, content.getFirst().cardId());
        assertEquals(LocalDate.of(2027, 3, 31), content.getFirst().expiryDate());
        assertEquals(CardsStatus.active, content.getFirst().cardStatus());
        assertEquals(new BigDecimal("100.00"), content.get(0).balance());
        assertEquals("**** **** **** **** 1234", content.get(0).maskedNumber());

        assertEquals(2L, content.get(1).cardId());
        assertEquals("**** **** **** **** 9876", content.get(1).maskedNumber());
    }

    @Test
    void findAllCardsUserByStatusAndLast4_shouldReturnPagedDto_andPassCorrectPageable() {
        // arrange
        Long userId = 10L;
        CardsStatus cardStatus = CardsStatus.active;
        String last4 = "1234";
        int page = 0;
        int size = 2;

        Card c1 = new Card();
        c1.setId(1L);
        c1.setLast4("1234");
        c1.setExpiryDate(LocalDate.of(2027, 3, 31));
        c1.setCardStatus(cardStatus);
        c1.setBalance(new BigDecimal("100.00"));

        Card c2 = new Card();
        c2.setId(2L);
        c2.setLast4("9876");
        c2.setExpiryDate(LocalDate.of(2028, 12, 31));
        c2.setCardStatus(CardsStatus.active);
        c2.setBalance(BigDecimal.ZERO);

        int totalElements = 5;
        PageRequest pr = PageRequest.of(page, size);
        Page<Card> cardsPage = new PageImpl<>(List.of(c1, c2), pr, totalElements);

        when(cardRepository.findAllByCardOwner_IdAndCardStatusAndLast4(eq(userId), eq(cardStatus), eq(last4), any(Pageable.class))).thenReturn(cardsPage);

        Page<UserReadCardResponse> result = cardService.findAllCardsUserByStatusAndLast4(userId, last4, cardStatus, page, size);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(cardRepository, times(1))
                .findAllByCardOwner_IdAndCardStatusAndLast4(eq(userId), eq(cardStatus), eq(last4), pageableCaptor.capture());

        Pageable passed = pageableCaptor.getValue();
        assertEquals(page, passed.getPageNumber());
        assertEquals(size, passed.getPageSize());

        // assert: page metadata preserved
        assertEquals(totalElements, result.getTotalElements());
        assertEquals((int) Math.ceil((double) totalElements / size), result.getTotalPages());
        assertEquals(2, result.getNumberOfElements());

        // assert: DTO mapping
        List<UserReadCardResponse> content = result.getContent();
        assertEquals(1L, content.getFirst().cardId());
        assertEquals(LocalDate.of(2027, 3, 31), content.getFirst().expiryDate());
        assertEquals(CardsStatus.active, content.getFirst().cardStatus());
        assertEquals(new BigDecimal("100.00"), content.get(0).balance());
        assertEquals("**** **** **** **** 1234", content.get(0).maskedNumber());

        assertEquals(2L, content.get(1).cardId());
        assertEquals("**** **** **** **** 9876", content.get(1).maskedNumber());
    }

    @Test
    void findAllUserCardsByStatus_shouldReturnPagedDto_andPassCorrectPageable() {
        // arrange
        Long userId = 10L;
        CardsStatus cardStatus = CardsStatus.active;
        int page = 0;
        int size = 2;

        Card c1 = new Card();
        c1.setId(1L);
        c1.setLast4("1234");
        c1.setExpiryDate(LocalDate.of(2027, 3, 31));
        c1.setCardStatus(cardStatus);
        c1.setBalance(new BigDecimal("100.00"));

        Card c2 = new Card();
        c2.setId(2L);
        c2.setLast4("9876");
        c2.setExpiryDate(LocalDate.of(2028, 12, 31));
        c2.setCardStatus(CardsStatus.active);
        c2.setBalance(BigDecimal.ZERO);

        int totalElements = 5;
        PageRequest pr = PageRequest.of(page, size);
        Page<Card> cardsPage = new PageImpl<>(List.of(c1, c2), pr, totalElements);

        when(cardRepository.findAllByCardOwner_IdAndCardStatus( eq(userId),eq(cardStatus), any(Pageable.class))).thenReturn(cardsPage);

        Page<UserReadCardResponse> result = cardService.findAllUserCardsByStatus(userId, cardStatus, page, size);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(cardRepository, times(1))
                .findAllByCardOwner_IdAndCardStatus( eq(userId), eq(cardStatus), pageableCaptor.capture());

        Pageable passed = pageableCaptor.getValue();
        assertEquals(page, passed.getPageNumber());
        assertEquals(size, passed.getPageSize());

        // assert: page metadata preserved
        assertEquals(totalElements, result.getTotalElements());
        assertEquals((int) Math.ceil((double) totalElements / size), result.getTotalPages());
        assertEquals(2, result.getNumberOfElements());

        // assert: DTO mapping
        List<UserReadCardResponse> content = result.getContent();
        assertEquals(1L, content.getFirst().cardId());
        assertEquals(LocalDate.of(2027, 3, 31), content.getFirst().expiryDate());
        assertEquals(CardsStatus.active, content.getFirst().cardStatus());
        assertEquals(new BigDecimal("100.00"), content.get(0).balance());
        assertEquals("**** **** **** **** 1234", content.get(0).maskedNumber());

        assertEquals(2L, content.get(1).cardId());
        assertEquals("**** **** **** **** 9876", content.get(1).maskedNumber());
    }
}

