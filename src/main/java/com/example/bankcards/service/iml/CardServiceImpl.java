package com.example.bankcards.service.iml;

import com.example.bankcards.dto.card.AdminCardDto;
import com.example.bankcards.dto.card.CardCreateRequestDto;
import com.example.bankcards.dto.user.UserReadCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequests;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.BlockRequestStatus;
import com.example.bankcards.entity.enums.CardsStatus;
import com.example.bankcards.exception.NotfoundCardException;
import com.example.bankcards.exception.NotfoundUserException;
import com.example.bankcards.repository.CardBlockRequestsRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final AesGcmPanCryptoService  panCryptoService;
    private final CardBlockRequestsRepository cardBlockRequestsRepository;

    @Transactional
    @Override
    public Card findCardByIdForTransfer(Long id, Long userId) {
        return cardRepository.findByIdAndOwnerForUpdate(id, userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AdminCardDto> findAllCards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllByDeletedFalseOrderByCreatedAtDesc(pageable);
        return cardsPage.map(Mapper::fromCardToAdminCardDto);
    }

    @Transactional
    @Override
    public AdminCardDto createCard(CardCreateRequestDto request) {
        User user = userRepository.findById(request.ownerId()).orElseThrow(() -> new NotfoundUserException("User with id " + request.ownerId() + " not found"));
        String pan = request.pan().trim();

        Card card = new Card();
        card.setCardOwner(user);
        card.setPanEnc(panCryptoService.encrypt(pan));
        card.setLast4(pan.substring(pan.length() - 4));
        card.setExpiryDate(request.expiryDate());

        Card saved = cardRepository.save(card);

        return Mapper.fromCardToAdminCardDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public AdminCardDto getCardById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new NotfoundCardException("Card with id " + id + " not found"));
        return Mapper.fromCardToAdminCardDto(card);
    }

    @Transactional
    @Override
    public AdminCardDto activateCard(Long id, Long userId) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new NotfoundCardException("Card with id " + id + " not found"));

        if(!card.getCardOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Card does not belong to user");
        }

        if (card.getExpiryDate().isBefore(OffsetDateTime.now().toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card is expired");
        }

        card.setCardStatus(CardsStatus.active);
        cardRepository.save(card);

        return Mapper.fromCardToAdminCardDto(card);
    }

    @Transactional
    @Override
    public AdminCardDto blockCard(Long id, Long userId) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new NotfoundCardException("Card with id " + id + " not found"));

        if(!card.getCardOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Card does not belong to user");
        }

        CardBlockRequests req = cardBlockRequestsRepository.findTopByCardIdOrderByCreatedAtDesc(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No block request for this card"));

        if (req.getStatus() != BlockRequestStatus.pending) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Block request is not pending");
        }

        req.setStatus(BlockRequestStatus.approved);
        req.setProcessedAt(OffsetDateTime.now());
        card.setCardStatus(CardsStatus.blocked);
        cardBlockRequestsRepository.save(req);
        cardRepository.save(card);


        return Mapper.fromCardToAdminCardDto(card);
    }

    @Transactional
    @Override
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NotfoundCardException("Card with id " + id + " not found"));

        if (card.getCardStatus() == CardsStatus.active) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Active card cannot be deleted");
        }

        if (card.getBalance() != null && card.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card balance must be zero to delete");
        }

        card.setDeleted(true);
        cardRepository.save(card);
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getBalance(Long cardId, Long userId) {
        Card card = cardRepository.findByIdAndCardOwner_IdAndDeletedFalse(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));

        return card.getBalance();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserReadCardResponse> findAllCardsUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllByCardOwner_IdAndDeletedFalse(userId, pageable);
        return cardsPage.map(Mapper::fromCardToUserReadCardResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserReadCardResponse> findAllCardsUserByStatusAndLast4(Long userId, String last4, CardsStatus cardStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardPage = cardRepository.findAllByCardOwner_IdAndCardStatusAndLast4AndDeletedFalse(userId, cardStatus, last4, pageable);
        return cardPage.map(Mapper::fromCardToUserReadCardResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserReadCardResponse> findAllUserCardsByStatus(Long userId, CardsStatus cardStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllByCardOwner_IdAndCardStatusAndDeletedFalse(userId, cardStatus, pageable);
        return cardsPage.map(Mapper::fromCardToUserReadCardResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserReadCardResponse> findAllUserCardsByLast4(Long userId, String last4, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAllByCardOwner_IdAndLast4AndDeletedFalse(userId, last4, pageable);
        return cardsPage.map(Mapper::fromCardToUserReadCardResponse);
    }
}
