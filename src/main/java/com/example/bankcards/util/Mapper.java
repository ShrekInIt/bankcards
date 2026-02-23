package com.example.bankcards.util;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;

import java.time.OffsetDateTime;

public class Mapper {
    public static UserReadCardResponse fromCardToUserReadCardResponse(Card card) {
        return UserReadCardResponse.fromLast4(card.getId(), card.getLast4(), card.getExpiryDate(),
                card.getCardStatus(), card.getBalance(), card.getCardOwner().getId());
    }

    public static UserResponseDto fromUserToUserResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(),
                user.getUserStatus(), user.getCreatedAt(), user.isActive());
    }

    public static User fromUserCredentialsDtoToUser(UserCredentialsDto dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setCreatedAt(OffsetDateTime.now());
        return user;
    }

    public static void applyUserUpdate(User user, UserUpdateDto dto) {
        if (dto.name() != null) user.setName(dto.name());
        if (dto.email() != null) user.setEmail(dto.email().trim().toLowerCase());
        if (dto.userStatus() != null) user.setUserStatus(dto.userStatus());
        if (dto.isActive() != null) user.setActive(dto.isActive());
    }

    public static AdminCardDto fromCardToAdminCardDto(Card c) {
        var o = c.getCardOwner();
        return new AdminCardDto(
                c.getId(),
                "**** **** **** ****" + c.getLast4(),
                c.getExpiryDate(),
                c.getCreatedAt(),
                c.getCardStatus(),
                c.getBalance(),
                o.getId(),
                o.getName()
        );
    }
}
