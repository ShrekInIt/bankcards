package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.UsersStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    private String name;
    private String email;
    private UsersStatus userStatus;
    private Boolean isActive;
}
