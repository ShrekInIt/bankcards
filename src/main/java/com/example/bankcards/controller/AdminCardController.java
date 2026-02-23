package com.example.bankcards.controller;

import com.example.bankcards.dto.card.AdminCardDto;
import com.example.bankcards.dto.card.CardCreateRequestDto;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RestController
@Slf4j
@RequestMapping("/admin/cards")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<AdminCardDto> createCard(@Valid @RequestBody CardCreateRequestDto request) {
        AdminCardDto created = cardService.createCard(request);
        return ResponseEntity
                .created(URI.create("/admin/cards/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminCardDto> getCard(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<AdminCardDto> activate(@PathVariable @Positive Long id, @RequestBody @Positive Long userId) {
        return ResponseEntity.ok(cardService.activateCard(id, userId));
    }

    @PatchMapping("/{cardId}/blocked")
    public ResponseEntity<AdminCardDto> blocked(@PathVariable @Positive Long cardId, @RequestBody @Positive Long userId) {
        return ResponseEntity.ok(cardService.blockCard(cardId, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable @Positive Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public Page<AdminCardDto> getCards(@RequestParam(defaultValue = "0")  @Min(0) int page,
                                       @RequestParam(defaultValue = "10") @Min(1) @Max(100)  int size) {

        return cardService.findAllCards(page, size);
    }
}
