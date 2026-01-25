package com.backend.promptvprompt.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_turns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GamePhase phase;

    @Column(nullable = false)
    private Integer turnNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String playerMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiResponse;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
