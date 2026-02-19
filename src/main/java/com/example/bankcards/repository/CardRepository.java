package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardsStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findAllByCardOwner_Id(Long userId, Pageable pageable);
    Page<Card> findAllByCardOwner_IdAndCardStatus(Long userId, CardsStatus status, Pageable pageable);
    Page<Card> findAllByCardOwner_IdAndLast4(Long userId, String last4, Pageable pageable);
    Page<Card> findAllByCardOwner_IdAndCardStatusAndLast4(Long userId, CardsStatus status, String last4, Pageable pageable);


    Optional<Card> findByIdAndCardOwner_Id(Long cardId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :cardId AND c.cardOwner.id = :userId")
    Optional<Card> findByIdAndOwnerForUpdate(Long cardId, Long userId);

    @EntityGraph(attributePaths = "cardOwner")
    Page<Card> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
