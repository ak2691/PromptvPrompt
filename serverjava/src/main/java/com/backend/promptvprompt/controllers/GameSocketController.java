package com.backend.promptvprompt.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.backend.promptvprompt.DTO.Matchmaking.GameFoundResponse;
import com.backend.promptvprompt.DTO.Matchmaking.JoinGameRoomRequest;
import com.backend.promptvprompt.DTO.Matchmaking.JoinQueueRequest;
import com.backend.promptvprompt.DTO.Matchmaking.Match;
import com.backend.promptvprompt.DTO.Matchmaking.QueueJoinedResponse;
import com.backend.promptvprompt.models.Game;
import com.backend.promptvprompt.services.GameService;
import com.backend.promptvprompt.services.MatchmakingService;

public class GameSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MatchmakingService matchmakingService;

    @Autowired
    private GameService gameService;

    @MessageMapping("/joinQueue")
    public void joinQueue(@Payload JoinQueueRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String userId = request.getUserId();

        Match match = matchmakingService.addPlayer(userId, sessionId);

        if (match != null) {
            // Create game from match
            Game game = gameService.createGameFromMatch(
                    match.getPlayerOne().getPlayerId(),
                    match.getPlayerTwo().getPlayerId());

            // Send game found event to both players
            GameFoundResponse response = new GameFoundResponse(game);

            messagingTemplate.convertAndSendToUser(
                    match.getPlayerOne().getSocketId(),
                    "/queue/gameFound",
                    response);

            messagingTemplate.convertAndSendToUser(
                    match.getPlayerTwo().getSocketId(),
                    "/queue/gameFound",
                    response);

        } else {
            // Send queue joined event
            QueueJoinedResponse response = new QueueJoinedResponse(
                    matchmakingService.getQueueSize());
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/queueJoined",
                    response);
        }
    }

    @MessageMapping("/joinGameRoom")
    public void joinGameRoom(@Payload JoinGameRoomRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String gameId = request.getGameId();
        String userId = request.getUserId();

        Game game = gameService.getGame(gameId);

        if (game != null &&
                (userId.equals(game.getPlayerOne().getId()) || userId.equals(game.getPlayerTwo().getId()))) {

            // Subscribe user to game-specific topic
            System.out.println("Socket " + sessionId + " joined game-" + gameId);

            // Note: Room joining in STOMP is handled by client subscribing to
            // /topic/game-{gameId}
            // You can also track this server-side if needed
        }
    }
}
