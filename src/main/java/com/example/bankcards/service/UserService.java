package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotfoundUserException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.jwt.JwtService;
import com.example.bankcards.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email){
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new NotfoundUserException("User with email " + email + " not found");
        }

        User user = optionalUser.get();
        return Mapper.fromUserToUserResponseDto(user);
    }

    @Transactional
    public String addUser(UserCredentialsDto userCredentialsDto){
        if (userRepository.existsByEmail(userCredentialsDto.email())) {
            throw new IllegalArgumentException("User with email " + userCredentialsDto.email() + " already exists");
        }

        User user = Mapper.fromUserCredentialsDtoToUser(userCredentialsDto);
        user.setPasswordHash(passwordEncoder.encode(userCredentialsDto.password()));
        userRepository.save(user);
        return "User registered successfully";
    }

    private User findByCredentials(UserCredentialsDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        User user = findByCredentials(userCredentialsDto);
        return jwtService.generateAuthToken(user.getEmail(), user.getUserStatus().name());
    }

    @Transactional(readOnly = true)
    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws AuthenticationException {
        String refreshToken = refreshTokenDto.refreshToken();
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            UserEmailDto userEmailInfo = getUserEmailInfo(jwtService.getEmailFromToken(refreshToken));
            return jwtService.refreshBaseToken(userEmailInfo.email(), userEmailInfo.role().name(), refreshTokenDto.refreshToken());
        }
        throw new AuthenticationException("Invalid refresh Token");
    }

    private UserEmailDto getUserEmailInfo(String email) {
        return userRepository.findUserEmailDtoByEmail(email)
                .orElseThrow(() -> new NotfoundUserException("User with email " + email + " not found"));
    }
}
