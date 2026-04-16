package com.example.bankcards.service;

import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.iml.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private static Card card(Long id, BigDecimal balance, CardsStatus status) {
        User owner = new User();
        owner.setId(77L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        Card card = new Card();
        card.setId(id);
        card.setCardOwner(owner);
        card.setBalance(balance);
        card.setCardStatus(status);
        card.setLast4(String.valueOf(id).substring(Math.max(0, String.valueOf(id).length() - 4)));
        card.setExpiryDate(LocalDate.of(2030, 12, 31));
        return card;
    }

    @Test
    void createTransaction_shouldTransferMoneyAndSaveTransaction() {
        TransferRequest request = new TransferRequest(20L, 10L, new BigDecimal("25.50"), "invoice");
        Card card10 = card(10L, new BigDecimal("50.00"), CardsStatus.active);
        Card card20 = card(20L, new BigDecimal("100.00"), CardsStatus.active);

        when(cardService.findCardByIdForTransfer(10L, 77L)).thenReturn(card10);
        when(cardService.findCardByIdForTransfer(20L, 77L)).thenReturn(card20);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(99L);
            return tx;
        });

        TransferResponse response = transactionService.createTransaction(request, 77L);

        assertEquals(99L, response.transactionId());
        assertEquals(20L, response.fromCardId());
        assertEquals(10L, response.toCardId());
        assertEquals(new BigDecimal("25.50"), response.amount());
        assertEquals("invoice", response.comment());
        assertEquals(new BigDecimal("74.50"), card20.getBalance());
        assertEquals(new BigDecimal("75.50"), card10.getBalance());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals(card20, captor.getValue().getFromCard());
        assertEquals(card10, captor.getValue().getToCard());
    }

    @Test
    void createTransaction_shouldThrowBadRequest_whenCardIdsAreNull() {
        TransferRequest request = new TransferRequest(null, 10L, new BigDecimal("1.00"), null);

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> transactionService.createTransaction(request, 77L));
        verifyNoInteractions(cardService, transactionRepository);
    }

    @Test
    void createTransaction_shouldThrowBadRequest_whenAmountIsZeroOrNegative() {
        TransferRequest request = new TransferRequest(10L, 20L, BigDecimal.ZERO, null);
        Card card10 = card(10L, new BigDecimal("50.00"), CardsStatus.active);
        Card card20 = card(20L, new BigDecimal("50.00"), CardsStatus.active);

        when(cardService.findCardByIdForTransfer(10L, 77L)).thenReturn(card10);
        when(cardService.findCardByIdForTransfer(20L, 77L)).thenReturn(card20);

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> transactionService.createTransaction(request, 77L));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_shouldThrowBadRequest_whenSameCardIsUsed() {
        TransferRequest request = new TransferRequest(10L, 10L, new BigDecimal("1.00"), null);
        Card card10 = card(10L, new BigDecimal("50.00"), CardsStatus.active);

        when(cardService.findCardByIdForTransfer(10L, 77L)).thenReturn(card10);

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> transactionService.createTransaction(request, 77L));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_shouldThrowBadRequest_whenInsufficientFunds() {
        TransferRequest request = new TransferRequest(10L, 20L, new BigDecimal("100.00"), null);
        Card card10 = card(10L, new BigDecimal("50.00"), CardsStatus.active);
        Card card20 = card(20L, new BigDecimal("50.00"), CardsStatus.active);

        when(cardService.findCardByIdForTransfer(10L, 77L)).thenReturn(card10);
        when(cardService.findCardByIdForTransfer(20L, 77L)).thenReturn(card20);

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> transactionService.createTransaction(request, 77L));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_shouldThrowBadRequest_whenCardBlocked() {
        TransferRequest request = new TransferRequest(10L, 20L, new BigDecimal("1.00"), null);
        Card card10 = card(10L, new BigDecimal("50.00"), CardsStatus.blocked);
        Card card20 = card(20L, new BigDecimal("50.00"), CardsStatus.active);

        when(cardService.findCardByIdForTransfer(10L, 77L)).thenReturn(card10);
        when(cardService.findCardByIdForTransfer(20L, 77L)).thenReturn(card20);

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> transactionService.createTransaction(request, 77L));
        verify(transactionRepository, never()).save(any());
    }
}
