package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.card.BlockRequestDto;
import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.security.CustomUserService;
import com.example.bankcards.security.jwt.JwtFilter;
import com.example.bankcards.security.jwt.JwtService;
import com.example.bankcards.service.CardBlockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserCardController.class)
@Import({SecurityConfig.class, JwtFilter.class, GlobalExceptionHandler.class})
class UserCardControllerTest extends ControllerMockMvcTest {

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

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private CardBlockRequestService cardBlockRequestsService;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void myCards_withoutFilters_shouldReturnAllCards() throws Exception {
        UserResponseDto currentUser = userResponseDto(77L);
        var page = pageOf(java.util.List.of(userCardDto("1234", CardsStatus.active, new BigDecimal("100.00"))), 1);

        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser);
        when(cardService.findAllCardsUser(77L, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/user/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardId").value(1))
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** **** 1234"))
                .andExpect(jsonPath("$.content[0].cardStatus").value(CardsStatus.active.name()));

        verify(userService).getUserByEmail("alice@example.com");
        verify(cardService).findAllCardsUser(77L, 0, 10);
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void myCards_withLast4_shouldFilterByLast4() throws Exception {
        UserResponseDto currentUser = userResponseDto(77L);
        var page = pageOf(java.util.List.of(userCardDto("1234", CardsStatus.active, new BigDecimal("100.00"))), 1);

        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser);
        when(cardService.findAllUserCardsByLast4(77L, "1234", 0, 10)).thenReturn(page);

        mockMvc.perform(get("/user/cards").param("last4", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** **** 1234"));

        verify(userService).getUserByEmail("alice@example.com");
        verify(cardService).findAllUserCardsByLast4(77L, "1234", 0, 10);
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void myCards_withStatus_shouldFilterByStatus() throws Exception {
        UserResponseDto currentUser = userResponseDto(77L);
        var page = pageOf(java.util.List.of(userCardDto("5678", CardsStatus.blocked, BigDecimal.ZERO)), 1);

        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser);
        when(cardService.findAllUserCardsByStatus(77L, CardsStatus.blocked, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/user/cards").param("status", "blocked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardStatus").value(CardsStatus.blocked.name()));

        verify(userService).getUserByEmail("alice@example.com");
        verify(cardService).findAllUserCardsByStatus(77L, CardsStatus.blocked, 0, 10);
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void myCards_withStatusAndLast4_shouldUseCombinedFilter() throws Exception {
        UserResponseDto currentUser = userResponseDto(77L);
        var page = pageOf(java.util.List.of(userCardDto("1234", CardsStatus.active, new BigDecimal("100.00"))), 1);

        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser);
        when(cardService.findAllCardsUserByStatusAndLast4(77L, "1234", CardsStatus.active, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/user/cards")
                        .param("last4", "1234")
                        .param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardId").value(1));

        verify(userService).getUserByEmail("alice@example.com");
        verify(cardService).findAllCardsUserByStatusAndLast4(77L, "1234", CardsStatus.active, 0, 10);
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void blockCard_shouldReturnBlockRequestId() throws Exception {
        UserResponseDto currentUser = userResponseDto(77L);
        BlockRequestDto request = new BlockRequestDto("card lost");
        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser);
        when(cardBlockRequestsService.addRequest(5L, 77L, "card lost")).thenReturn(99L);

        mockMvc.perform(post("/user/cards/{cardId}/block-requests", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("99"));

        verify(userService).getUserByEmail("alice@example.com");
        verify(cardBlockRequestsService).addRequest(5L, 77L, "card lost");
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void transaction_shouldReturnTransferResponse() throws Exception {
        UserResponseDto currentUser = userResponseDto(77L);
        TransferRequest request = new TransferRequest(11L, 22L, new BigDecimal("25.50"), "invoice");
        TransferResponse response = new TransferResponse(123L, 11L, 22L, new BigDecimal("25.50"), CREATED_AT, "invoice");

        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser);
        when(transactionService.createTransaction(eq(request), eq(77L))).thenReturn(response);

        mockMvc.perform(post("/user/cards/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(123))
                .andExpect(jsonPath("$.amount").value(25.50));

        verify(userService).getUserByEmail("alice@example.com");
        verify(transactionService).createTransaction(eq(request), eq(77L));
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    void balance_shouldReturnBalance() throws Exception {
        UserResponseDto currentUser = userResponseDto(77L);
        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser);
        when(cardService.getBalance(5L, 77L)).thenReturn(new BigDecimal("500.25"));

        mockMvc.perform(get("/user/cards/{cardId}/balance", 5L))
                .andExpect(status().isOk())
                .andExpect(content().string("500.25"));

        verify(userService).getUserByEmail("alice@example.com");
        verify(cardService).getBalance(5L, 77L);
    }

    @Test
    void myCards_withoutAuthentication_shouldReturn403() throws Exception {
        mockMvc.perform(get("/user/cards"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService, cardService, cardBlockRequestsService, transactionService);
    }
}




