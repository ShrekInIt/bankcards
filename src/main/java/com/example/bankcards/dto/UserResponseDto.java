package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.UsersStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private UsersStatus userStatus;
    private OffsetDateTime createdAt;
    private boolean isActive;
}
