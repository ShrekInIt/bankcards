package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.UsersStatus;

public record UserEmailDto(String email, UsersStatus role) {}
