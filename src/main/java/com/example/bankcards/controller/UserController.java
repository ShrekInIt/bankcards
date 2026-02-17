package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockRequestDto;
import com.example.bankcards.dto.UserReadCardResponse;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.service.CardBlockRequestsService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final CardService cardService;
    private final CardBlockRequestsService cardBlockRequestsService;

    @GetMapping("/cards")
    public Page<UserReadCardResponse> myCards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String last4,
            @RequestParam(required = false) CardsStatus status
    ){
        String email = checkUser(authentication);

        Long userId = userService.getUserByEmail(email).getId();

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
        Long userId = userService.getUserByEmail(email).getId();
        return cardBlockRequestsService.addRequest(cardId, userId, dto.reason());
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
}
