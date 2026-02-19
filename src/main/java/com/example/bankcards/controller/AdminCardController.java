package com.example.bankcards.controller;

import com.example.bankcards.dto.AdminCardDto;
import com.example.bankcards.service.CardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Slf4j
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCardController {

    private final CardService cardService;

    @GetMapping("/cards")
    public Page<AdminCardDto> getCards(@RequestParam(defaultValue = "0")  @Min(0) int page,
                                       @RequestParam(defaultValue = "10") @Min(1) @Max(100)  int size) {

        return cardService.findAllCards(page, size);
    }

}
