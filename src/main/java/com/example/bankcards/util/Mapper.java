package com.example.bankcards.util;

import com.example.bankcards.dto.UserCredentialsDto;
import com.example.bankcards.dto.UserReadCardResponse;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UsersStatus;

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

    public static User fromUserCredentialsDtoToUser(UserCredentialsDto userDto) {
        User user = new User();

        user.setName(userDto.name());
        user.setEmail(userDto.email());
        user.setActive(true);
        user.setPasswordHash(userDto.password());
        user.setUserStatus(UsersStatus.user);
        user.setCreatedAt(OffsetDateTime.now());

        return user;
    }
}
