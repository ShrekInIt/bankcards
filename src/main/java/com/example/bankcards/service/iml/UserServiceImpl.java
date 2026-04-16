package com.example.bankcards.service.iml;

import com.example.bankcards.dto.jwt.JwtAuthenticationDto;
import com.example.bankcards.dto.jwt.RefreshTokenDto;
import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.user.UserEmailDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.dto.user.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotfoundUserException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.jwt.JwtService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.Mapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotfoundUserException("User with email " + email + " not found"));
        return Mapper.fromUserToUserResponseDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotfoundUserException("User with id " + id + " not found"));
        return Mapper.fromUserToUserResponseDto(user);
    }

    @Transactional
    @Override
    public UserResponseDto createUser(UserCredentialsDto userCreateDto) {
        if (userRepository.existsByEmail(userCreateDto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email " + userCreateDto.email() + " already exists");
        }

        User user = Mapper.fromUserCredentialsDtoToUser(userCreateDto);

        userRepository.save(user);

        return Mapper.fromUserToUserResponseDto(user);
    }

    @Transactional
    @Override
    public UserResponseDto updateUser(Long id, UserUpdateDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotfoundUserException("User with id " + id + " not found"));

        if (request.email() != null) {
            String email = request.email().trim().toLowerCase();
            boolean exists = userRepository.existsByEmailAndIdNot(email, id);
            if (exists) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        Mapper.applyUserUpdate(user, request);

        userRepository.save(user);
        return Mapper.fromUserToUserResponseDto(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotfoundUserException("User with id " + id + " not found"));

        userRepository.delete(user);
    }

    @Transactional
    @Override
    public String addUser(UserCredentialsDto userCredentialsDto){
        if (userRepository.existsByEmail(userCredentialsDto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email " + userCredentialsDto.email() + " already exists");
        }

        User user = Mapper.fromUserCredentialsDtoToUser(userCredentialsDto);
        user.setPasswordHash(passwordEncoder.encode(userCredentialsDto.password()));
        userRepository.save(user);
        return "User registered successfully";
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserResponseDto> findAllUsers(@Min(0) int page, @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAllByOrderByCreatedAtDesc(pageable);
        return usersPage.map(Mapper::fromUserToUserResponseDto);
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
    @Override
    public JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDto) {
        User user = findByCredentials(userCredentialsDto);
        return jwtService.generateAuthToken(user.getEmail(), user.getUserStatus().name());
    }

    @Transactional(readOnly = true)
    @Override
    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) {
        String refreshToken = refreshTokenDto.refreshToken();
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            UserEmailDto userEmailInfo = getUserEmailInfo(jwtService.getEmailFromToken(refreshToken));
            return jwtService.refreshBaseToken(userEmailInfo.email(), userEmailInfo.role().name(), refreshTokenDto.refreshToken());
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    private UserEmailDto getUserEmailInfo(String email) {
        return userRepository.findUserEmailDtoByEmail(email)
                .orElseThrow(() -> new NotfoundUserException("User with email " + email + " not found"));
    }
}
