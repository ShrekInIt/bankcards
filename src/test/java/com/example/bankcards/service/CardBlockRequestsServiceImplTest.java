package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequests;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.BlockRequestStatus;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.repository.CardBlockRequestsRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.iml.CardBlockRequestsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardBlockRequestsServiceImplTest {

    @Mock
    private CardBlockRequestsRepository cardBlockRequestsRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardBlockRequestsServiceImpl service;

    private static User owner(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        return user;
    }

    private static Card card(Long id, Long ownerId, CardsStatus status) {
        Card card = new Card();
        card.setId(id);
        card.setCardOwner(owner(ownerId));
        card.setCardStatus(status);
        card.setLast4("1234");
        card.setExpiryDate(LocalDate.of(2030, 12, 31));
        card.setCreatedAt(OffsetDateTime.parse("2026-01-15T10:15:30Z"));
        return card;
    }

    @Test
    void addRequest_shouldSavePendingRequest_andReturnId() {
        Card existingCard = card(5L, 77L, CardsStatus.active);
        when(cardBlockRequestsRepository.existsByCard_IdAndStatus(5L, BlockRequestStatus.pending)).thenReturn(false);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(existingCard));
        when(cardBlockRequestsRepository.save(any(CardBlockRequests.class))).thenAnswer(invocation -> {
            CardBlockRequests request = invocation.getArgument(0);
            request.setId(42L);
            return request;
        });

        Long result = service.addRequest(5L, 77L, "card lost");

        assertEquals(42L, result);
        ArgumentCaptor<CardBlockRequests> captor = ArgumentCaptor.forClass(CardBlockRequests.class);
        verify(cardBlockRequestsRepository).save(captor.capture());
        assertEquals(existingCard, captor.getValue().getCard());
        assertEquals("card lost", captor.getValue().getReason());
        assertEquals(BlockRequestStatus.pending, captor.getValue().getStatus());
    }

    @Test
    void addRequest_shouldThrowConflict_whenPendingRequestAlreadyExists() {
        when(cardBlockRequestsRepository.existsByCard_IdAndStatus(6L, BlockRequestStatus.pending)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.addRequest(6L, 77L, "card lost"));

        assertEquals(409, ex.getStatusCode().value());
        verify(cardRepository, never()).findById(any());
        verify(cardBlockRequestsRepository, never()).save(any());
    }

    @Test
    void addRequest_shouldThrowNotFound_whenCardMissing() {
        when(cardBlockRequestsRepository.existsByCard_IdAndStatus(7L, BlockRequestStatus.pending)).thenReturn(false);
        when(cardRepository.findById(7L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.addRequest(7L, 77L, "card lost"));

        assertEquals(404, ex.getStatusCode().value());
        verify(cardBlockRequestsRepository, never()).save(any());
    }

    @Test
    void addRequest_shouldThrowForbidden_whenCardBelongsToAnotherUser() {
        when(cardBlockRequestsRepository.existsByCard_IdAndStatus(8L, BlockRequestStatus.pending)).thenReturn(false);
        when(cardRepository.findById(8L)).thenReturn(Optional.of(card(8L, 100L, CardsStatus.active)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.addRequest(8L, 77L, "card lost"));

        assertEquals(403, ex.getStatusCode().value());
        verify(cardBlockRequestsRepository, never()).save(any());
    }

    @Test
    void addRequest_shouldThrowConflict_whenCardAlreadyBlocked() {
        when(cardBlockRequestsRepository.existsByCard_IdAndStatus(9L, BlockRequestStatus.pending)).thenReturn(false);
        when(cardRepository.findById(9L)).thenReturn(Optional.of(card(9L, 77L, CardsStatus.blocked)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.addRequest(9L, 77L, "card lost"));

        assertEquals(409, ex.getStatusCode().value());
        verify(cardBlockRequestsRepository, never()).save(any());
    }

    @Test
    void addRequest_shouldThrowBadRequest_whenReasonIsNull() {
        when(cardBlockRequestsRepository.existsByCard_IdAndStatus(10L, BlockRequestStatus.pending)).thenReturn(false);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card(10L, 77L, CardsStatus.active)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.addRequest(10L, 77L, null));

        assertEquals(400, ex.getStatusCode().value());
        verify(cardBlockRequestsRepository, never()).save(any());
    }
}
