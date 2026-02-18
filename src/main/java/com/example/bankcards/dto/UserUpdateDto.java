package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.UsersStatus;

public record UserUpdateDto(String name, String email, UsersStatus userStatus, Boolean isActive) {}
