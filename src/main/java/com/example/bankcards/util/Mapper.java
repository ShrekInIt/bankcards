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
        UserReadCardResponse response = new UserReadCardResponse();
        response.setCardId(card.getId());
        response.setBalance(card.getBalance());
        response.setCardStatus(card.getCardStatus());
        response.setExpiryDate(card.getExpiryDate());
        response.setMaskedNumber("**** **** **** **** " + card.getLast4());
        response.setUserId(card.getCardOwner().getId());
        return response;
    }

    public static UserResponseDto fromUserToUserResponseDto(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setName(user.getName());
        userResponseDto.setId(user.getId());
        userResponseDto.setUserStatus(user.getUserStatus());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setCreatedAt(user.getCreatedAt());
        userResponseDto.setActive(user.isActive());
        return userResponseDto;
    }

    public static User fromUserCredentialsDtoToUser(UserCredentialsDto userDto) {
        User user = new User();

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setActive(true);
        user.setPasswordHash(userDto.getPassword());
        user.setUserStatus(UsersStatus.user);
        user.setCreatedAt(OffsetDateTime.now());

        return user;
    }
}
