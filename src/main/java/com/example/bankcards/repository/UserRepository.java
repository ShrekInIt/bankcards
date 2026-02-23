package com.example.bankcards.repository;

import com.example.bankcards.dto.user.UserEmailDto;
import com.example.bankcards.entity.User;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT new com.example.bankcards.dto.user.UserEmailDto(u.email, u.userStatus) " +
            "FROM User u WHERE u.email = :email")
    Optional<UserEmailDto> findUserEmailDtoByEmail(@Param("email") String email);

    boolean existsByEmailAndIdNot(String email, @Positive(message = "id") Long id);

    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
