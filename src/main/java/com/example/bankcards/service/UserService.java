package com.example.bankcards.service;

import com.example.bankcards.dto.jwt.JwtAuthenticationDto;
import com.example.bankcards.dto.jwt.RefreshTokenDto;
import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.dto.user.UserUpdateDto;
import org.springframework.data.domain.Page;

/**
 * Сервис для работы с пользователями и аутентификацией.
 */
public interface UserService {

    /**
     * Возвращает пользователя по адресу электронной почты.
     *
     * @param email email пользователя
     * @return данные пользователя
     */
    UserResponseDto getUserByEmail(String email);

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return данные пользователя
     */
    UserResponseDto getUserById(Long id);

    /**
     * Создаёт нового пользователя.
     *
     * @param userCreateDto данные пользователя
     * @return созданный пользователь
     */
    UserResponseDto createUser(UserCredentialsDto userCreateDto);

    /**
     * Обновляет данные пользователя.
     *
     * @param id идентификатор пользователя
     * @param request новые данные пользователя
     * @return обновлённые данные пользователя
     */
    UserResponseDto updateUser(Long id, UserUpdateDto request);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     */
    void deleteUser(Long id);

    /**
     * Регистрирует нового пользователя.
     *
     * @param userCredentialsDto регистрационные данные
     * @return служебное сообщение о результате операции
     */
    String addUser(UserCredentialsDto userCredentialsDto);

    /**
     * Возвращает список пользователей с постраничным выводом.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @return страница с пользователями
     */
    Page<UserResponseDto> findAllUsers(int page, int size);

    /**
     * Выполняет вход пользователя в систему.
     *
     * @param userCredentialsDto данные для входа
     * @return токены аутентификации
     */
    JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDto);

    /**
     * Обновляет токен доступа по refresh token.
     *
     * @param refreshTokenDto данные refresh token
     * @return обновлённые токены аутентификации
     */
    JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto);
}
