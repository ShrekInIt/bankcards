package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.dto.user.UserUpdateDto;
import com.example.bankcards.entity.enums.UsersStatus;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
@Import({SecurityConfig.class, JwtFilter.class, GlobalExceptionHandler.class})
class AdminUserControllerTest extends ControllerMockMvcTest {

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
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturn201_andLocationHeader() throws Exception {
        UserCredentialsDto request = new UserCredentialsDto("Alice", "alice@example.com", "password123");
        when(userService.createUser(any(UserCredentialsDto.class))).thenReturn(userResponseDto(15L));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/admin/users/15"))
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(userService).createUser(eq(request));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_withInvalidBody_shouldReturn400() throws Exception {
        UserCredentialsDto request = new UserCredentialsDto("A", "bad-email", "123");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_shouldReturn200_andBody() throws Exception {
        when(userService.getUserById(15L)).thenReturn(userResponseDto(15L));

        mockMvc.perform(get("/admin/users/{id}", 15L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.active").value(true));

        verify(userService).getUserById(15L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_shouldReturn200_andBody() throws Exception {
        UserUpdateDto request = new UserUpdateDto("Alice Updated", "alice.updated@example.com", UsersStatus.ADMIN, true);
        when(userService.updateUser(eq(15L), any(UserUpdateDto.class)))
                .thenReturn(new UserResponseDto(15L, "Alice Updated", "alice.updated@example.com", UsersStatus.ADMIN, CREATED_AT, true));

        mockMvc.perform(patch("/admin/users/{id}", 15L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"))
                .andExpect(jsonPath("$.userStatus").value("ADMIN"));

        verify(userService).updateUser(eq(15L), eq(request));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/users/{id}", 15L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(15L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_shouldReturnPagedUsers() throws Exception {
        var page = pageOf(java.util.List.of(userResponseDto(1L), userResponseDto(2L)), 2);
        when(userService.findAllUsers(0, 10)).thenReturn(page);

        mockMvc.perform(get("/admin/users/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(2));

        verify(userService).findAllUsers(0, 10);
    }
}


