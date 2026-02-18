package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT c FROM Card c WHERE c.cardOwner.id = :userId")
    Page<Card> findAllUserCardsByUserId(@Param("userId")Long userId,
                                        Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.cardOwner.id = :userId AND c.cardStatus = :cardStatus AND c.last4 = :last4")
    Page<Card> findAllUserCardsByCardStatusAndCardLast4(
            @Param("cardStatus")CardsStatus cardStatus,
            @Param("last4")String last4,
            @Param("userId")Long userId,
            Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.cardOwner.id = :userId AND c.last4 = :last4")
    Page<Card> findAllUserCardsByCardLast4(
            @Param("last4")String last4,
            @Param("userId")Long userId,
            Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.cardOwner.id = :userId AND c.cardStatus = :cardStatus")
    Page<Card> findAllUserCardsByCardStatus(
            @Param("cardStatus")CardsStatus cardStatus,
            @Param("userId")Long userId,
            Pageable pageable);

    Optional<Card> findByIdAndCardOwner_Id(Long cardId, Long userId);
}
