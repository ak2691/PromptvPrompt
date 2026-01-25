package com.backend.promptvprompt.DTO.Matchmaking;

import com.backend.promptvprompt.models.Game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameFoundResponse {
    private Game game;

}
