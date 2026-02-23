package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCredentialsDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RestController
@Slf4j
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCredentialsDto request) {
        UserResponseDto created = userService.createUser(request);

        return ResponseEntity
                .created(URI.create("/admin/users/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable @Positive(message = "id must be positive") Long id) {
        UserResponseDto userResponseDto = userService.getUserById(id);
        return ResponseEntity.ok(userResponseDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable @Positive(message = "id must be positive") Long id, @Valid @RequestBody UserUpdateDto request) {
        UserResponseDto userResponseDto = userService.updateUser(id, request);
        return ResponseEntity.ok(userResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public Page<UserResponseDto> getUsers(@RequestParam(defaultValue = "0")  @Min(0) int page,
                                       @RequestParam(defaultValue = "10") @Min(1) @Max(100)  int size) {

        return userService.findAllUsers(page, size);
    }
}
