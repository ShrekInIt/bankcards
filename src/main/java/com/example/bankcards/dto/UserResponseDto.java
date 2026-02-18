package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.UsersStatus;

import java.time.OffsetDateTime;

public record UserResponseDto(Long id, String name, String email, UsersStatus userStatus, OffsetDateTime createdAt, boolean isActive){}
