package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.card.CardCreateRequestDto;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.security.CustomUserService;
import com.example.bankcards.security.jwt.JwtFilter;
import com.example.bankcards.security.jwt.JwtService;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

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

@WebMvcTest(controllers = AdminCardController.class)
@Import({SecurityConfig.class, JwtFilter.class, GlobalExceptionHandler.class})
class AdminCardControllerTest extends ControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockitoBean
    private JwtService jwtService;

    @SuppressWarnings("unused")
    @MockitoBean
    private CustomUserService customUserService;

    @MockitoBean
    private CardService cardService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_shouldReturn201_andLocationHeader() throws Exception {
        CardCreateRequestDto request = new CardCreateRequestDto(77L, "1234567812345678", LocalDate.of(2030, 12, 31));
        var created = adminCardDto(10L);

        when(cardService.createCard(any(CardCreateRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/admin/cards/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.maskedPan").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.cardStatus").value(CardsStatus.active.name()))
                .andExpect(jsonPath("$.ownerId").value(77));

        verify(cardService).createCard(eq(request));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_withInvalidBody_shouldReturn400() throws Exception {
        CardCreateRequestDto request = new CardCreateRequestDto(77L, "123", LocalDate.of(2030, 12, 31));

        mockMvc.perform(post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cardService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCard_shouldReturn200_andCardDto() throws Exception {
        when(cardService.getCardById(5L)).thenReturn(adminCardDto(5L));

        mockMvc.perform(get("/admin/cards/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.ownerUsername").value("alice"));

        verify(cardService).getCardById(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activate_shouldReturn200_andBody() throws Exception {
        when(cardService.activateCard(5L, 77L)).thenReturn(adminCardDto(5L));

        mockMvc.perform(patch("/admin/cards/{id}/activate", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(cardService).activateCard(5L, 77L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blocked_shouldReturn200_andBody() throws Exception {
        when(cardService.blockCard(5L, 77L)).thenReturn(adminCardDto(5L));

        mockMvc.perform(patch("/admin/cards/{cardId}/blocked", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(cardService).blockCard(5L, 77L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/cards/{id}", 5L))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCards_shouldReturnPagedCards() throws Exception {
        var page = pageOf(java.util.List.of(adminCardDto(1L), adminCardDto(2L)), 2);
        when(cardService.findAllCards(0, 10)).thenReturn(page);

        mockMvc.perform(get("/admin/cards/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(2));

        verify(cardService).findAllCards(0, 10);
    }
}


