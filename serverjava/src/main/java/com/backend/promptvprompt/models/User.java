package com.backend.promptvprompt.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean isEmailVerified = false;

    private String verificationToken;

    private LocalDateTime verificationExpiry;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    @Builder.Default
    private List<GameTurn> gameTurns = new ArrayList<>();

    @OneToMany(mappedBy = "playerOne")
    @Builder.Default
    private List<Game> playerOneGames = new ArrayList<>();

    @OneToMany(mappedBy = "playerTwo")
    @Builder.Default
    private List<Game> playerTwoGames = new ArrayList<>();

    @OneToMany(mappedBy = "winner")
    @Builder.Default
    private List<Game> wonGames = new ArrayList<>();
}
