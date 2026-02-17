package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.UsersStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UsersStatus userStatus = UsersStatus.user;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "cardOwner", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Card> userCards = new ArrayList<>();

}

