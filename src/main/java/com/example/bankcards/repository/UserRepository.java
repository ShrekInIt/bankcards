package com.example.bankcards.repository;

import com.example.bankcards.dto.UserEmailDto;
import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT new com.example.bankcards.dto.UserEmailDto(u.email, u.userStatus) " +
            "FROM User u WHERE u.email = :email")
    Optional<UserEmailDto> findUserEmailDtoByEmail(@Param("email") String email);
}
