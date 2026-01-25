package com.backend.promptvprompt.DTO.Matchmaking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerQueue {
    private String playerId;
    private String socketId;
    private Long joinedAt;
}
