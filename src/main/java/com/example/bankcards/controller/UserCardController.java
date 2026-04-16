package com.example.bankcards.controller;

import com.example.bankcards.dto.card.BlockRequestDto;
import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.dto.user.UserReadCardResponse;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.CardBlockRequestService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequestMapping("/user")
public class UserCardController {

    private final UserService userService;
    private final CardService cardService;
    private final CardBlockRequestService cardBlockRequestsService;
    private final TransactionService transactionService;

    @GetMapping("/cards")
    public Page<UserReadCardResponse> myCards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String last4,
            @RequestParam(required = false) CardsStatus status
    ){
        String email = checkUser(authentication);

        Long userId = userService.getUserByEmail(email).id();

        if ((last4 == null || last4.isEmpty()) && (status == null)) {
            return cardService.findAllCardsUser(userId, page, size);
        } else if ((last4 != null && !last4.isEmpty()) && (status == null)) {
            return cardService.findAllUserCardsByLast4(userId, last4, page, size);
        } else if(last4 == null || last4.isEmpty()) {
            return cardService.findAllUserCardsByStatus(userId, status, page, size);
        } else {
            return cardService.findAllCardsUserByStatusAndLast4(userId, last4, status, page, size);
        }
    }

    @PostMapping("/cards/{cardId}/block-requests")
    public Long blockCard(Authentication authentication,
                          @PathVariable Long cardId,
                          @RequestBody BlockRequestDto dto) {

        String email = checkUser(authentication);
        Long userId = userService.getUserByEmail(email).id();
        return invokeAddRequest(cardBlockRequestsService, cardId, userId, dto.reason());
    }

    @PostMapping("/cards/transfers")
    public ResponseEntity<TransferResponse> transaction(Authentication authentication,
                                         @RequestBody TransferRequest dto
    ) {
        String email = checkUser(authentication);
        Long userId = userService.getUserByEmail(email).id();

        TransferResponse response = transactionService.createTransaction(dto, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cards/{cardId}/balance")
    public BigDecimal balanceCard(Authentication authentication,
                                  @PathVariable Long cardId
    ) {

        String email = checkUser(authentication);
        Long userId = userService.getUserByEmail(email).id();

        return cardService.getBalance(cardId, userId);
    }

    private String checkUser(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return email;
    }

    private Long invokeAddRequest(CardBlockRequestService service, Long cardId, Long userId, String reason) {
        try {
            return (Long) service.getClass()
                    .getMethod("addRequest", Long.class, Long.class, String.class)
                    .invoke(service, cardId, userId, reason);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to add block request", e);
        }
    }
}
