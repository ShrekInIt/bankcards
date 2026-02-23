package com.example.bankcards.dto.user;

import com.example.bankcards.entity.enums.UsersStatus;

public record UserEmailDto(String email, UsersStatus role) {}
