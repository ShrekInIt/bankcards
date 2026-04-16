package com.example.bankcards.controller;

import com.example.bankcards.dto.card.AdminCardDto;
import com.example.bankcards.dto.user.UserReadCardResponse;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.entity.enums.UsersStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

abstract class ControllerMockMvcTest {

    protected static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-01-15T10:15:30Z");

    @Autowired
    protected com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    protected String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected static <T> Page<T> pageOf(List<T> content, long totalElements) {
        return new PageImpl<>(content, PageRequest.of(0, 10), totalElements);
    }

    protected static AdminCardDto adminCardDto(Long id) {
        return new AdminCardDto(
                id,
                "**** **** **** 1234",
                LocalDate.of(2030, 12, 31),
                CREATED_AT,
                CardsStatus.active,
                new BigDecimal("150.00"),
                77L,
                "alice"
        );
    }

    protected static UserResponseDto userResponseDto(Long id) {
        return new UserResponseDto(
                id,
                "Alice",
                "alice@example.com",
                UsersStatus.USER,
                CREATED_AT,
                true
        );
    }

    protected static UserReadCardResponse userCardDto(String last4, CardsStatus status, BigDecimal balance) {
        return new UserReadCardResponse(
                1L,
                "**** **** **** **** " + last4,
                LocalDate.of(2030, 12, 31),
                status,
                balance,
                77L
        );
    }
}
