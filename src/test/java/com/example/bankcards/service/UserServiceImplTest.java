package com.example.bankcards.service;

import com.example.bankcards.dto.jwt.JwtAuthenticationDto;
import com.example.bankcards.dto.jwt.RefreshTokenDto;
import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.user.UserEmailDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.dto.user.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UsersStatus;
import com.example.bankcards.exception.NotfoundUserException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.jwt.JwtService;
import com.example.bankcards.service.iml.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private static User user(Long id, String name, String email, UsersStatus status, boolean active, String passwordHash) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setUserStatus(status);
        user.setActive(active);
        user.setPasswordHash(passwordHash);
        user.setCreatedAt(OffsetDateTime.parse("2026-01-15T10:15:30Z"));
        return user;
    }

    @Test
    void getUserByEmail_shouldReturnDto_whenUserExists() {
        User entity = user(1L, "Alice", "alice@example.com", UsersStatus.USER, true, "hash");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(entity));

        UserResponseDto result = userService.getUserByEmail("alice@example.com");

        assertEquals(1L, result.id());
        assertEquals("Alice", result.name());
        assertEquals("alice@example.com", result.email());
        assertEquals(UsersStatus.USER, result.userStatus());
        assertEquals(entity.getCreatedAt(), result.createdAt());
        assertTrue(result.active());
        verify(userRepository).findByEmail("alice@example.com");
    }

    @Test
    void getUserByEmail_shouldThrow_whenUserMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(NotfoundUserException.class, () -> userService.getUserByEmail("missing@example.com"));
        verify(userRepository).findByEmail("missing@example.com");
    }

    @Test
    void createUser_shouldThrowConflict_whenEmailAlreadyExists() {
        UserCredentialsDto request = new UserCredentialsDto("Alice", "alice@example.com", "password123");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.createUser(request));

        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository).existsByEmail("alice@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldApplyChangesAndSave() {
        User existing = user(10L, "Alice", "alice@example.com", UsersStatus.USER, true, "hash");
        UserUpdateDto request = new UserUpdateDto("Alice Updated", "alice.updated@example.com", UsersStatus.ADMIN, false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("alice.updated@example.com", 10L)).thenReturn(false);

        UserResponseDto result = userService.updateUser(10L, request);

        assertEquals(10L, result.id());
        assertEquals("Alice Updated", result.name());
        assertEquals("alice.updated@example.com", result.email());
        assertEquals(UsersStatus.ADMIN, result.userStatus());
        assertFalse(result.active());
        verify(userRepository).save(existing);
        assertEquals("alice.updated@example.com", existing.getEmail());
        assertEquals(UsersStatus.ADMIN, existing.getUserStatus());
        assertFalse(existing.isActive());
    }

    @Test
    void updateUser_shouldThrowConflict_whenEmailInUseByAnotherUser() {
        User existing = user(10L, "Alice", "alice@example.com", UsersStatus.USER, true, "hash");
        UserUpdateDto request = new UserUpdateDto("Alice Updated", "alice.updated@example.com", UsersStatus.ADMIN, false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("alice.updated@example.com", 10L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.updateUser(10L, request));

        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldDeleteExistingUser() {
        User existing = user(10L, "Alice", "alice@example.com", UsersStatus.USER, true, "hash");
        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));

        userService.deleteUser(10L);

        verify(userRepository).delete(existing);
    }

    @Test
    void addUser_shouldEncodePasswordAndReturnMessage() {
        UserCredentialsDto request = new UserCredentialsDto("Alice", "alice@example.com", "password123");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        String result = userService.addUser(request);

        assertEquals("User registered successfully", result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Alice", captor.getValue().getName());
        assertEquals("alice@example.com", captor.getValue().getEmail());
        assertEquals("encoded-password", captor.getValue().getPasswordHash());
    }

    @Test
    void signIn_shouldReturnJwtTokens_whenCredentialsValid() {
        User entity = user(1L, "Alice", "alice@example.com", UsersStatus.ADMIN, true, "hash");
        UserCredentialsDto request = new UserCredentialsDto("Alice", "alice@example.com", "password123");
        JwtAuthenticationDto expected = new JwtAuthenticationDto("token", "refresh");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(jwtService.generateAuthToken("alice@example.com", "ADMIN")).thenReturn(expected);

        JwtAuthenticationDto result = userService.signIn(request);

        assertEquals(expected, result);
        verify(jwtService).generateAuthToken("alice@example.com", "ADMIN");
    }

    @Test
    void signIn_shouldThrowBadCredentials_whenPasswordMismatch() {
        User entity = user(1L, "Alice", "alice@example.com", UsersStatus.ADMIN, true, "hash");
        UserCredentialsDto request = new UserCredentialsDto("Alice", "alice@example.com", "wrong-password");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("wrong-password", "hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.signIn(request));
        verify(jwtService, never()).generateAuthToken(any(), any());
    }

    @Test
    void refreshToken_shouldReturnNewTokens_whenRefreshTokenValid() {
        RefreshTokenDto request = new RefreshTokenDto("refresh-token");
        when(jwtService.validateJwtToken("refresh-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("refresh-token")).thenReturn("alice@example.com");
        when(userRepository.findUserEmailDtoByEmail("alice@example.com")).thenReturn(Optional.of(new UserEmailDto("alice@example.com", UsersStatus.USER)));
        JwtAuthenticationDto expected = new JwtAuthenticationDto("new-token", "refresh-token");
        when(jwtService.refreshBaseToken("alice@example.com", "USER", "refresh-token")).thenReturn(expected);

        JwtAuthenticationDto result = userService.refreshToken(request);

        assertEquals(expected, result);
        verify(jwtService).refreshBaseToken("alice@example.com", "USER", "refresh-token");
    }

    @Test
    void refreshToken_shouldThrowUnauthorized_whenTokenInvalid() {
        RefreshTokenDto request = new RefreshTokenDto("bad-token");
        when(jwtService.validateJwtToken("bad-token")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.refreshToken(request));

        assertEquals(401, ex.getStatusCode().value());
        verifyNoInteractions(userRepository);
    }

    @Test
    void findAllUsers_shouldReturnPagedDtos() {
        User u1 = user(1L, "Alice", "alice@example.com", UsersStatus.USER, true, "hash");
        User u2 = user(2L, "Bob", "bob@example.com", UsersStatus.ADMIN, false, "hash2");
        Page<User> page = new PageImpl<>(List.of(u1, u2), PageRequest.of(0, 10), 2);
        when(userRepository.findAllByOrderByCreatedAtDesc(any())).thenReturn(page);

        Page<UserResponseDto> result = userService.findAllUsers(0, 10);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getNumberOfElements());
        assertEquals("Alice", result.getContent().getFirst().name());
        assertEquals("Bob", result.getContent().get(1).name());
        verify(userRepository).findAllByOrderByCreatedAtDesc(any());
    }
}



