package com.backend.promptvprompt.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.backend.promptvprompt.models.Game;
import com.backend.promptvprompt.models.ScenarioTemplate;

@Repository
public interface GameRepo extends JpaRepository<Game, String> {
    @Query("SELECT g FROM Game g LEFT JOIN FETCH g.template LEFT JOIN FETCH g.turns t WHERE g.id = :gameId ORDER BY t.createdAt ASC")
    Optional<Game> findByIdWithTemplateAndTurns(String gameId);
}
