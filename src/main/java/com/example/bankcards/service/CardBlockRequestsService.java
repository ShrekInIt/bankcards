package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequests;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.BlockRequestStatus;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.repository.CardBlockRequestsRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class CardBlockRequestsService {

    private final CardBlockRequestsRepository cardBlockRequestsRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long addRequest(Long cardId, Long userId, String reason) {
        if (cardBlockRequestsRepository.existsByCard_IdAndStatus(cardId, BlockRequestStatus.pending)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pending request for card " + cardId + " already exists");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card " + cardId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + userId + " not found"));

        if (!card.getCardOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Card does not belong to user");
        }

        if (card.getCardStatus() == CardsStatus.blocked) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card is already blocked");
        }

        CardBlockRequests req = new CardBlockRequests();
        req.setCard(card);
        req.setUser(user);
        req.setReason(reason);
        req.setStatus(BlockRequestStatus.pending);

        cardBlockRequestsRepository.save(req);
        return req.getId();
    }
}
