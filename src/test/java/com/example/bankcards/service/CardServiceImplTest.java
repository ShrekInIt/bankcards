package com.example.bankcards.service;

import com.example.bankcards.dto.card.AdminCardDto;
import com.example.bankcards.dto.card.CardCreateRequestDto;
import com.example.bankcards.dto.user.UserReadCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequests;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.BlockRequestStatus;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.entity.enums.UsersStatus;
import com.example.bankcards.exception.NotfoundCardException;
import com.example.bankcards.exception.NotfoundUserException;
import com.example.bankcards.repository.CardBlockRequestsRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.iml.AesGcmPanCryptoService;
import com.example.bankcards.service.iml.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AesGcmPanCryptoService panCryptoService;

    @Mock
    private CardBlockRequestsRepository cardBlockRequestsRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private static User user(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(name.toLowerCase() + "@example.com");
        u.setUserStatus(UsersStatus.USER);
        u.setPasswordHash("hash");
        u.setActive(true);
        u.setCreatedAt(OffsetDateTime.parse("2026-01-15T10:15:30Z"));
        return u;
    }

    private static Card card(User owner, CardsStatus status, BigDecimal balance, LocalDate expiry) {
        Card c = new Card();
        c.setId(5L);
        c.setCardOwner(owner);
        c.setCardStatus(status);
        c.setBalance(balance);
        c.setExpiryDate(expiry);
        c.setLast4("1234");
        c.setCreatedAt(OffsetDateTime.parse("2026-01-15T10:15:30Z"));
        c.setPanEnc(new byte[]{1, 2, 3});
        c.setDeleted(false);
        return c;
    }

    @Test
    void createCard_shouldEncryptPanSetLast4AndReturnDto() {
        User owner = user(77L, "Alice");
        CardCreateRequestDto request = new CardCreateRequestDto(77L, " 1234567812345678 ", LocalDate.of(2030, 12, 31));

        when(userRepository.findById(77L)).thenReturn(Optional.of(owner));
        when(panCryptoService.encrypt("1234567812345678")).thenReturn(new byte[]{9, 9, 9});
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        AdminCardDto dto = cardService.createCard(request);

        assertEquals(10L, dto.id());
        assertEquals(77L, dto.ownerId());
        assertEquals(CardsStatus.active, dto.cardStatus());

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(captor.capture());
        Card saved = captor.getValue();
        assertEquals("5678", saved.getLast4());
        assertEquals(owner, saved.getCardOwner());
        assertEquals(LocalDate.of(2030, 12, 31), saved.getExpiryDate());
        verify(panCryptoService).encrypt("1234567812345678");
    }

    @Test
    void createCard_shouldThrow_whenOwnerNotFound() {
        CardCreateRequestDto request = new CardCreateRequestDto(77L, "1234567812345678", LocalDate.of(2030, 12, 31));
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(NotfoundUserException.class, () -> cardService.createCard(request));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCardById_shouldReturnDto() {
        User owner = user(77L, "Alice");
        Card card = card(owner, CardsStatus.active, new BigDecimal("100.00"), LocalDate.of(2030, 12, 31));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        AdminCardDto dto = cardService.getCardById(5L);

        assertEquals(5L, dto.id());
        assertEquals("Alice", dto.ownerUsername());
    }

    @Test
    void getCardById_shouldThrow_whenCardMissing() {
        when(cardRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotfoundCardException.class, () -> cardService.getCardById(5L));
    }

    @Test
    void activateCard_shouldSetStatusActive_andSave() {
        User owner = user(77L, "Alice");
        Card card = card(owner, CardsStatus.blocked, BigDecimal.ZERO, LocalDate.now().plusDays(10));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        AdminCardDto dto = cardService.activateCard(5L, 77L);

        assertEquals(CardsStatus.active, card.getCardStatus());
        assertEquals(CardsStatus.active, dto.cardStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void activateCard_shouldThrow403_whenNotOwner() {
        User owner = user(100L, "Bob");
        Card card = card(owner, CardsStatus.blocked, BigDecimal.ZERO, LocalDate.now().plusDays(10));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> cardService.activateCard(5L, 77L));

        assertEquals(403, ex.getStatusCode().value());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void activateCard_shouldThrow409_whenExpired() {
        User owner = user(77L, "Alice");
        Card card = card(owner, CardsStatus.blocked, BigDecimal.ZERO, LocalDate.now().minusDays(1));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> cardService.activateCard(5L, 77L));

        assertEquals(409, ex.getStatusCode().value());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockCard_shouldApproveRequestAndBlockCard() {
        User owner = user(77L, "Alice");
        Card card = card(owner, CardsStatus.active, BigDecimal.ZERO, LocalDate.now().plusDays(20));
        CardBlockRequests req = new CardBlockRequests();
        req.setId(100L);
        req.setCard(card);
        req.setUser(owner);
        req.setStatus(BlockRequestStatus.pending);

        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));
        when(cardBlockRequestsRepository.findTopByCardIdOrderByCreatedAtDesc(5L)).thenReturn(Optional.of(req));

        AdminCardDto dto = cardService.blockCard(5L, 77L);

        assertEquals(CardsStatus.blocked, card.getCardStatus());
        assertEquals(BlockRequestStatus.approved, req.getStatus());
        assertNotNull(req.getProcessedAt());
        assertEquals(CardsStatus.blocked, dto.cardStatus());
        verify(cardBlockRequestsRepository).save(req);
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_shouldThrow409_whenNoRequest() {
        User owner = user(77L, "Alice");
        Card card = card(owner, CardsStatus.active, BigDecimal.ZERO, LocalDate.now().plusDays(20));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));
        when(cardBlockRequestsRepository.findTopByCardIdOrderByCreatedAtDesc(5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> cardService.blockCard(5L, 77L));

        assertEquals(409, ex.getStatusCode().value());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void deleteCard_shouldSoftDelete_whenAllowed() {
        User owner = user(77L, "Alice");
        Card card = card(owner, CardsStatus.blocked, BigDecimal.ZERO, LocalDate.now().plusDays(20));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        cardService.deleteCard(5L);

        assertTrue(card.isDeleted());
        verify(cardRepository).save(card);
    }

    @Test
    void deleteCard_shouldThrow409_whenActive() {
        User owner = user(77L, "Alice");
        Card card = card(owner, CardsStatus.active, BigDecimal.ZERO, LocalDate.now().plusDays(20));
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> cardService.deleteCard(5L));

        assertEquals(409, ex.getStatusCode().value());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getBalance_shouldReturnBalance() {
        User owner = user(77L, "Alice");
        Card card = card(owner, CardsStatus.active, new BigDecimal("123.45"), LocalDate.now().plusDays(20));
        when(cardRepository.findByIdAndCardOwner_IdAndDeletedFalse(5L, 77L)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getBalance(5L, 77L);

        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    void findAllCardsUser_shouldMapToUserReadCardResponse() {
        User owner = user(77L, "Alice");
        Card card = card(owner, CardsStatus.active, new BigDecimal("123.45"), LocalDate.now().plusDays(20));
        Page<Card> page = new PageImpl<>(List.of(card), PageRequest.of(0, 10), 1);
        when(cardRepository.findAllByCardOwner_IdAndDeletedFalse(77L, PageRequest.of(0, 10))).thenReturn(page);

        Page<UserReadCardResponse> result = cardService.findAllCardsUser(77L, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().getFirst().cardId());
        assertEquals(CardsStatus.active, result.getContent().getFirst().cardStatus());
    }
}

