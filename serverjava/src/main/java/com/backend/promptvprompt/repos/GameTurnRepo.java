package com.backend.promptvprompt.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.promptvprompt.models.GamePhase;
import com.backend.promptvprompt.models.GameTurn;

@Repository
public interface GameTurnRepo extends JpaRepository<GameTurn, String> {

    int countByGameIdAndPlayerIdAndPhase(String gameId, String playerId, GamePhase phase);

    List<GameTurn> findByGameIdAndPlayerIdAndPhaseOrderByTurnNumberAsc(
            String gameId, String playerId, GamePhase phase);
}
