package com.backend.promptvprompt.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.promptvprompt.models.Game;
import com.backend.promptvprompt.models.GameEndReason;
import com.backend.promptvprompt.models.GamePhase;
import com.backend.promptvprompt.models.GameStatus;
import com.backend.promptvprompt.models.GameTurn;
import com.backend.promptvprompt.models.ScenarioTemplate;
import com.backend.promptvprompt.models.User;
import com.backend.promptvprompt.repos.GameRepo;
import com.backend.promptvprompt.repos.GameTurnRepo;
import com.backend.promptvprompt.repos.ScenarioTemplateRepo;
import com.backend.promptvprompt.repos.UserRepo;
import com.backend.promptvprompt.services.AiService;
import com.backend.promptvprompt.services.GameService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameService Tests")
class GameServiceTest {

        @Mock
        private GameRepo gameRepo;

        @Mock
        private GameTurnRepo gameTurnRepo;

        @Mock
        private AiService aiService;

        @Mock
        private UserRepo userRepo;

        @Mock
        private ScenarioTemplateRepo scenarioTemplateRepo;

        @InjectMocks
        private GameService gameService;

        @BeforeEach
        void setUp() {
                // Reset mocks before each test if needed
        }

        @Nested
        @DisplayName("createGame")
        class CreateGameTests {

                @Test
                @DisplayName("should create a new game with correct initial state")
                void shouldCreateGameWithCorrectInitialState() {
                        // Arrange
                        ScenarioTemplate mockTemplate = ScenarioTemplate.builder()
                                        .id("template-1")
                                        .name("Teenager Gossip")
                                        .characterTemplate("A teenager with a secret")
                                        .build();
                        User playerOne = User.builder()
                                        .id("player-1")
                                        .build();

                        User playerTwo = User.builder()
                                        .id("player-2")
                                        .build();
                        Game mockGame = Game.builder()
                                        .id("game-1")
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .generatedCharacter("A suspicious guard")
                                        .generatedSecret("The password is blue42")
                                        .status(GameStatus.DEFENSE_PHASE)
                                        .phase(GamePhase.DEFENSE)
                                        .maxTurnsPerPhase(5)
                                        .maxCharsPerMessage(250)
                                        .build();

                        // Mock the methods (assuming you'll need to spy on the service for these)
                        GameService spyService = spy(gameService);
                        doReturn(mockTemplate).when(spyService).generateTemplate();
                        doReturn("guard").when(spyService).generateCharacter(mockTemplate);
                        doReturn("password").when(spyService).generateSecret(mockTemplate);
                        when(gameRepo.save(any(Game.class))).thenReturn(mockGame);
                        when(userRepo.findById("player-1")).thenReturn(Optional.of(playerOne));
                        when(userRepo.findById("player-2")).thenReturn(Optional.of(playerTwo));

                        // Act
                        Game result = spyService.createGameFromMatch("player-1", "player-2");

                        // Assert

                        assertEquals(GameStatus.DEFENSE_PHASE, result.getStatus());
                        assertEquals(GamePhase.DEFENSE, result.getPhase());
                        assertEquals("player-1", result.getPlayerOne().getId());
                        assertEquals("player-2", result.getPlayerTwo().getId());
                }
        }

        @Nested
        @DisplayName("validateTurn")
        class ValidateTurnTests {
                User playerOne = User.builder()
                                .id("player-1")
                                .build();

                User playerTwo = User.builder()
                                .id("player-2")
                                .build();

                @Test
                @DisplayName("should throw error if game is not in progress")
                void shouldThrowErrorIfGameNotInProgress() {
                        // Arrange

                        Game game = Game.builder()
                                        .status(GameStatus.COMPLETED)
                                        .maxCharsPerMessage(250)
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .build();

                        // Act & Assert
                        IllegalStateException exception = assertThrows(IllegalStateException.class,
                                        () -> gameService.validateTurn(game, "player-1", "Test message"));
                        assertEquals("Game not in progress", exception.getMessage());
                }

                @Test
                @DisplayName("should throw error if message exceeds character limit")
                void shouldThrowErrorIfMessageExceedsCharLimit() {
                        // Arrange
                        Game game = Game.builder()
                                        .status(GameStatus.ATTACK_PHASE)
                                        .maxCharsPerMessage(250)
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .build();

                        String longMessage = "a".repeat(251);

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                        () -> gameService.validateTurn(game, "player-1", longMessage));
                        assertEquals("Message exceeds 250 characters", exception.getMessage());
                }

                @Test
                @DisplayName("should throw error if player is not part of the game")
                void shouldThrowErrorIfPlayerNotInGame() {
                        // Arrange
                        Game game = Game.builder()
                                        .status(GameStatus.ATTACK_PHASE)
                                        .maxCharsPerMessage(250)
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .build();

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                        () -> gameService.validateTurn(game, "player-3", "Test"));
                        assertEquals("Player not in this game", exception.getMessage());
                }

                @Test
                @DisplayName("should not throw error for valid turn")
                void shouldNotThrowErrorForValidTurn() {
                        // Arrange
                        Game game = Game.builder()
                                        .status(GameStatus.ATTACK_PHASE)
                                        .maxCharsPerMessage(250)
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .build();

                        // Act & Assert
                        assertDoesNotThrow(() -> gameService.validateTurn(game, "player-1", "Valid message"));
                }
        }

        @Nested
        @DisplayName("getTurnCount")
        class GetTurnCountTests {

                @Test
                @DisplayName("should return correct turn count for player in specific phase")
                void shouldReturnCorrectTurnCount() {
                        // Arrange
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase(
                                        "game-1", "player-1", GamePhase.DEFENSE)).thenReturn(3);

                        // Act
                        long count = gameService.getTurnCount("game-1", "player-1", GamePhase.DEFENSE);

                        // Assert
                        verify(gameTurnRepo).countByGameIdAndPlayerIdAndPhase(
                                        "game-1", "player-1", GamePhase.DEFENSE);
                        assertEquals(3L, count);
                }

                @Test
                @DisplayName("should return 0 when player has no turns in phase")
                void shouldReturnZeroWhenNoTurns() {
                        // Arrange
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase(
                                        "game-1", "player-1", GamePhase.DEFENSE)).thenReturn(0);

                        // Act
                        long count = gameService.getTurnCount("game-1", "player-1", GamePhase.DEFENSE);

                        // Assert
                        assertEquals(0L, count);
                }
        }

        @Nested
        @DisplayName("checkPhaseTransition")
        class CheckPhaseTransitionTests {
                User playerOne = User.builder()
                                .id("player-1")
                                .build();

                User playerTwo = User.builder()
                                .id("player-2")
                                .build();

                @Test
                @DisplayName("should not transition if both players have not completed defense turns")
                void shouldNotTransitionIfTurnsIncomplete() {
                        // Arrange
                        Game mockGame = Game.builder()
                                        .id("game-1")
                                        .phase(GamePhase.DEFENSE)
                                        .maxTurnsPerPhase(5)
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .build();

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-1",
                                        GamePhase.DEFENSE))
                                        .thenReturn(4);
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-2",
                                        GamePhase.DEFENSE))
                                        .thenReturn(3);

                        GameService spyService = spy(gameService);

                        // Act
                        spyService.checkPhaseTransition("game-1");

                        // Assert
                        verify(spyService, never()).transitionToAttack(any());
                }

                @Test
                @DisplayName("should transition to ATTACK when both players complete defense turns")
                void shouldTransitionToAttackWhenTurnsComplete() {
                        // Arrange
                        ScenarioTemplate mockTemplate = new ScenarioTemplate();
                        Game mockGame = Game.builder()
                                        .id("game-1")
                                        .phase(GamePhase.DEFENSE)
                                        .maxTurnsPerPhase(5)
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .template(mockTemplate)
                                        .build();

                        when(gameRepo.findById("game-1"))
                                        .thenReturn(Optional.of(mockGame))
                                        .thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-1",
                                        GamePhase.DEFENSE))
                                        .thenReturn(5);
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-2",
                                        GamePhase.DEFENSE))
                                        .thenReturn(5);
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc(anyString(), anyString(),
                                        any()))
                                        .thenReturn(Collections.emptyList());
                        when(aiService.summarizeDefense(anyList())).thenReturn("Summary");
                        when(gameRepo.save(any(Game.class))).thenReturn(mockGame);

                        // Act
                        gameService.checkPhaseTransition("game-1");

                        // Assert
                        verify(gameRepo).save(argThat(game -> game.getIsTransitioning() &&
                                        game.getPhase() == GamePhase.ATTACK &&
                                        game.getPlayerOneDefenseSummary().equals("Summary") &&
                                        game.getPlayerTwoDefenseSummary().equals("Summary") &&
                                        game.getStatus() == GameStatus.ATTACK_PHASE &&
                                        game.getTransitionEndsAt() != null &&
                                        game.getTransitionEndsAt().isAfter(LocalDateTime.now().minusSeconds(1))));

                }

                @Test
                @DisplayName("should not transition if already in ATTACK phase")
                void shouldNotTransitionIfAlreadyInAttack() {
                        // Arrange
                        Game mockGame = Game.builder()
                                        .id("game-1")
                                        .phase(GamePhase.ATTACK)
                                        .maxTurnsPerPhase(5)
                                        .build();

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));

                        // Act
                        gameService.checkPhaseTransition("game-1");

                        // Assert
                        verify(gameTurnRepo, never()).countByGameIdAndPlayerIdAndPhase(anyString(), anyString(),
                                        any());
                }
        }

        @Nested
        @DisplayName("checkGameEnd")
        class CheckGameEndTests {
                User playerOne = User.builder()
                                .id("player-1")
                                .build();

                User playerTwo = User.builder()
                                .id("player-2")
                                .build();

                @Test
                @DisplayName("should not check game end if not in ATTACK phase")
                void shouldNotCheckIfNotInAttackPhase() {
                        // Arrange
                        Game mockGame = Game.builder()
                                        .id("game-1")
                                        .phase(GamePhase.DEFENSE)
                                        .build();

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));

                        // Act
                        gameService.checkGameEnd("game-1");

                        // Assert
                        verify(gameTurnRepo, never()).countByGameIdAndPlayerIdAndPhase(anyString(), anyString(),
                                        any());
                }

                @Test
                @DisplayName("should not end game if both players have not completed attack turns")
                void shouldNotEndGameIfTurnsIncomplete() {
                        // Arrange
                        Game mockGame = Game.builder()
                                        .id("game-1")
                                        .phase(GamePhase.ATTACK)
                                        .maxTurnsPerPhase(5)
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .build();

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-1",
                                        GamePhase.ATTACK))
                                        .thenReturn(5);
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-2",
                                        GamePhase.ATTACK))
                                        .thenReturn(3);

                        GameService spyService = spy(gameService);

                        // Act
                        spyService.checkGameEnd("game-1");

                        // Assert
                        verify(spyService, never()).determineWinner(anyString());
                }

                @Test
                @DisplayName("should determine winner when both players complete attack turns")
                void shouldDetermineWinnerWhenTurnsComplete() {
                        // Arrange
                        Game mockGame = Game.builder()
                                        .id("game-1")
                                        .phase(GamePhase.ATTACK)
                                        .maxTurnsPerPhase(5)
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .build();

                        when(gameRepo.findById("game-1"))
                                        .thenReturn(Optional.of(mockGame))
                                        .thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-1",
                                        GamePhase.ATTACK))
                                        .thenReturn(5);
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-2",
                                        GamePhase.ATTACK))
                                        .thenReturn(5);
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc(anyString(), anyString(),
                                        any()))
                                        .thenReturn(Collections.emptyList());
                        when(aiService.checkSecretRevealed(any(Game.class), anyList()))
                                        .thenReturn(true) // P1 succeeded
                                        .thenReturn(false); // P2 failed
                        when(gameRepo.save(any(Game.class))).thenReturn(mockGame);

                        // Act
                        gameService.checkGameEnd("game-1");

                        // Assert
                        verify(gameRepo).save(any(Game.class));
                }
        }

        @Nested
        @DisplayName("determineWinner")
        class DetermineWinnerTests {
                User playerOne = User.builder()
                                .id("player-1")
                                .build();

                User playerTwo = User.builder()
                                .id("player-2")
                                .build();

                private Game setupMockGame() {
                        return Game.builder()
                                        .id("game-1")
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .winner(null)
                                        .generatedSecret("secret123")
                                        .build();
                }

                @Test
                @DisplayName("should declare Player 1 winner when only P1 reveals secret")
                void shouldDeclarePlayer1WinnerWhenOnlyP1Reveals() {
                        // Arrange
                        Game mockGame = setupMockGame();
                        List<GameTurn> p1Turns = List.of(new GameTurn());
                        List<GameTurn> p2Turns = List.of(new GameTurn());

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc("game-1", "player-1",
                                        GamePhase.ATTACK))
                                        .thenReturn(p1Turns);
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc("game-1", "player-2",
                                        GamePhase.ATTACK))
                                        .thenReturn(p2Turns);
                        when(aiService.checkSecretRevealed(eq(mockGame), eq(p1Turns)))
                                        .thenReturn(true);
                        when(aiService.checkSecretRevealed(eq(mockGame), eq(p2Turns)))
                                        .thenReturn(false);
                        when(gameRepo.save(any(Game.class))).thenReturn(mockGame);

                        // Act
                        gameService.determineWinner("game-1");

                        // Assert
                        verify(gameRepo).save(argThat(game -> game.getStatus() == GameStatus.COMPLETED &&
                                        game.getWinner().getId().equals("player-1") &&
                                        game.getEndReason() == GameEndReason.FULL_CONVICTION));
                }

                @Test
                @DisplayName("should declare Player 2 winner when only P2 reveals secret")
                void shouldDeclarePlayer2WinnerWhenOnlyP2Reveals() {
                        // Arrange
                        Game mockGame = setupMockGame();
                        List<GameTurn> p1Turns = List.of(new GameTurn());
                        List<GameTurn> p2Turns = List.of(new GameTurn());

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc("game-1", "player-1",
                                        GamePhase.ATTACK))
                                        .thenReturn(p1Turns);
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc("game-1", "player-2",
                                        GamePhase.ATTACK))
                                        .thenReturn(p2Turns);
                        when(aiService.checkSecretRevealed(eq(mockGame), eq(p1Turns)))
                                        .thenReturn(false);
                        when(aiService.checkSecretRevealed(eq(mockGame), eq(p2Turns)))
                                        .thenReturn(true);
                        when(gameRepo.save(any(Game.class))).thenReturn(mockGame);

                        // Act
                        gameService.determineWinner("game-1");

                        // Assert
                        verify(gameRepo).save(argThat(game -> game.getStatus() == GameStatus.COMPLETED &&
                                        game.getWinner().getId().equals("player-2") &&
                                        game.getEndReason() == GameEndReason.FULL_CONVICTION));
                }

                @Test
                @DisplayName("should declare draw when both players reveal secret")
                void shouldDeclareDrawWhenBothReveal() {
                        // Arrange
                        Game mockGame = setupMockGame();
                        List<GameTurn> p1Turns = List.of(new GameTurn());
                        List<GameTurn> p2Turns = List.of(new GameTurn());

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc("game-1", "player-1",
                                        GamePhase.ATTACK))
                                        .thenReturn(p1Turns);
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc("game-1", "player-2",
                                        GamePhase.ATTACK))
                                        .thenReturn(p2Turns);
                        when(aiService.checkSecretRevealed(eq(mockGame), eq(p1Turns)))
                                        .thenReturn(true);
                        when(aiService.checkSecretRevealed(eq(mockGame), eq(p2Turns)))
                                        .thenReturn(true);
                        when(gameRepo.save(any(Game.class))).thenReturn(mockGame);

                        // Act
                        gameService.determineWinner("game-1");

                        // Assert
                        verify(gameRepo).save(argThat(game -> game.getStatus() == GameStatus.COMPLETED &&
                                        game.getWinner() == null &&
                                        game.getEndReason() == GameEndReason.DRAW));
                }

                @Test
                @DisplayName("should declare draw when neither player reveals secret")
                void shouldDeclareDrawWhenNeitherReveals() {
                        // Arrange
                        Game mockGame = setupMockGame();
                        List<GameTurn> p1Turns = List.of(new GameTurn());
                        List<GameTurn> p2Turns = List.of(new GameTurn());

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc("game-1", "player-1",
                                        GamePhase.ATTACK))
                                        .thenReturn(p1Turns);
                        when(gameTurnRepo.findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc("game-1", "player-2",
                                        GamePhase.ATTACK))
                                        .thenReturn(p2Turns);
                        when(aiService.checkSecretRevealed(eq(mockGame), eq(p1Turns)))
                                        .thenReturn(false);
                        when(aiService.checkSecretRevealed(eq(mockGame), eq(p2Turns)))
                                        .thenReturn(false);
                        when(gameRepo.save(any(Game.class))).thenReturn(mockGame);

                        // Act
                        gameService.determineWinner("game-1");

                        // Assert
                        verify(gameRepo).save(argThat(game -> game.getStatus() == GameStatus.COMPLETED &&
                                        game.getWinner() == null &&
                                        game.getEndReason() == GameEndReason.DRAW));
                }
        }

        @Nested
        @DisplayName("submitTurn")
        class SubmitTurnTests {
                User playerOne = User.builder()
                                .id("player-1")
                                .build();

                User playerTwo = User.builder()
                                .id("player-2")
                                .build();

                @Test
                @DisplayName("should throw error when turn limit is reached")
                void shouldThrowErrorWhenTurnLimitReached() {
                        // Arrange
                        Game mockGame = Game.builder()
                                        .id("game-1")
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .phase(GamePhase.DEFENSE)
                                        .maxTurnsPerPhase(5)
                                        .maxCharsPerMessage(250)
                                        .status(GameStatus.DEFENSE_PHASE)
                                        .template(new ScenarioTemplate())
                                        .build();

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-1",
                                        GamePhase.DEFENSE))
                                        .thenReturn(5);

                        // Act & Assert
                        IllegalStateException exception = assertThrows(IllegalStateException.class,
                                        () -> gameService.submitTurn("game-1", "player-1", "Test message"));
                        assertEquals("Turn limit reached", exception.getMessage());
                }

                @Test
                @DisplayName("should create turn and get AI response when valid")
                void shouldCreateTurnAndGetAiResponse() {
                        // Arrange
                        Game mockGame = Game.builder()
                                        .id("game-1")
                                        .playerOne(playerOne)
                                        .playerTwo(playerTwo)
                                        .phase(GamePhase.DEFENSE)
                                        .maxTurnsPerPhase(5)
                                        .maxCharsPerMessage(250)
                                        .status(GameStatus.DEFENSE_PHASE)
                                        .template(new ScenarioTemplate())
                                        .build();

                        GameTurn mockTurn = GameTurn.builder()
                                        .id("turn-1")
                                        .game(mockGame)
                                        .player(playerOne)
                                        .phase(GamePhase.DEFENSE)
                                        .turnNumber(3)
                                        .playerMessage("Test message")
                                        .aiResponse("AI says no!")
                                        .build();

                        when(gameRepo.findById("game-1")).thenReturn(Optional.of(mockGame));
                        when(gameTurnRepo.countByGameIdAndPlayerIdAndPhase("game-1", "player-1",
                                        GamePhase.DEFENSE))
                                        .thenReturn(2) // Current count for validation
                                        .thenReturn(3) // P1 turns for phase check
                                        .thenReturn(2); // P2 turns for phase check
                        when(aiService.getResponse(mockGame, "player-1", "Test message", GamePhase.DEFENSE))
                                        .thenReturn("AI says no!");
                        when(gameTurnRepo.save(any(GameTurn.class))).thenReturn(mockTurn);
                        when(userRepo.findById("player-1")).thenReturn(Optional.of(playerOne));
                        // when(userRepo.findById("player-2")).thenReturn(Optional.of(playerTwo));
                        // Act
                        GameTurn result = gameService.submitTurn("game-1", "player-1", "Test message");

                        // Assert
                        verify(aiService).getResponse(mockGame, "player-1", "Test message", GamePhase.DEFENSE);
                        verify(gameTurnRepo).save(argThat(turn -> turn.getGame().getId().equals("game-1") &&
                                        turn.getPlayer().getId().equals("player-1") &&
                                        turn.getPhase() == GamePhase.DEFENSE &&
                                        turn.getTurnNumber() == 3 &&
                                        turn.getPlayerMessage().equals("Test message") &&
                                        turn.getAiResponse().equals("AI says no!")));
                        assertEquals(mockTurn, result);
                }
        }
}
