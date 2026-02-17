package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBlockRequests;
import com.example.bankcards.entity.enums.BlockRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CardBlockRequestsRepository extends JpaRepository<CardBlockRequests, Long> {

    boolean existsByCard_IdAndStatus(Long cardId, BlockRequestStatus status);

}
