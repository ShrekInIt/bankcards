package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardService cardService;

    @Transactional
    public TransferResponse createTransaction(TransferRequest transferRequest, Long userId) {
        Long fromId = transferRequest.fromCardId();
        Long toId = transferRequest.toCardId();

        if (fromId == null || toId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is null");
        }

        Long firstId = fromId < toId ? fromId : toId;
        Long secondId = fromId < toId ? toId : fromId;

        Card first = cardService.findCardByIdForTransfer(firstId, userId);
        Card second = cardService.findCardByIdForTransfer(secondId, userId);

        Card cardFrom = fromId.equals(firstId) ? first : second;
        Card cardTo = toId.equals(firstId) ? first : second;

        if (transferRequest.amount() == null || transferRequest.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be > 0");
        }

        if (transferRequest.fromCardId().equals(transferRequest.toCardId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to same card");
        }

        if (cardFrom.getBalance().compareTo(transferRequest.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        if(cardFrom.getCardStatus() == CardsStatus.blocked || cardTo.getCardStatus() == CardsStatus.blocked ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card is blockedd");
        }

        OffsetDateTime now = OffsetDateTime.now();

        cardFrom.setBalance(cardFrom.getBalance().subtract(transferRequest.amount()));
        cardTo.setBalance(cardTo.getBalance().add(transferRequest.amount()));

        Transaction transaction = new Transaction();
        transaction.setFromCard(cardFrom);
        transaction.setToCard(cardTo);

        transaction.setAmount(transferRequest.amount());
        transaction.setCreatedAt(now);
        transaction.setComment(transferRequest.comment());

        transactionRepository.save(transaction);

        return new TransferResponse(transaction.getId(), cardFrom.getId(), cardTo.getId(), transferRequest.amount(), now, transferRequest.comment());
    }
}
