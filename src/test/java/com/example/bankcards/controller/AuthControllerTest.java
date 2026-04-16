package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.jwt.JwtAuthenticationDto;
import com.example.bankcards.dto.jwt.RefreshTokenDto;
import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.security.CustomUserService;
import com.example.bankcards.security.jwt.JwtFilter;
import com.example.bankcards.security.jwt.JwtService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtFilter.class, GlobalExceptionHandler.class})
class AuthControllerTest extends ControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockitoBean
    private JwtService jwtService;

    @SuppressWarnings("unused")
    @MockitoBean
    private CustomUserService customUserService;

    @MockitoBean
    private UserService userService;

    @Test
    void signUp_shouldReturn201_andPlainTextBody() throws Exception {
        UserCredentialsDto request = new UserCredentialsDto("Alice", "alice@example.com", "password123");
        when(userService.addUser(eq(request))).thenReturn("user-created");

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("user-created"));

        verify(userService).addUser(eq(request));
    }

    @Test
    void signIn_shouldReturn200_andTokens() throws Exception {
        UserCredentialsDto request = new UserCredentialsDto("Alice", "alice@example.com", "password123");
        JwtAuthenticationDto response = new JwtAuthenticationDto("access-token", "refresh-token");
        when(userService.signIn(eq(request))).thenReturn(response);

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(userService).signIn(eq(request));
    }

    @Test
    void refresh_shouldReturn200_andTokens() throws Exception {
        RefreshTokenDto request = new RefreshTokenDto("refresh-token");
        JwtAuthenticationDto response = new JwtAuthenticationDto("new-access-token", "new-refresh-token");
        when(userService.refreshToken(eq(request))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        verify(userService).refreshToken(eq(request));
    }
}

