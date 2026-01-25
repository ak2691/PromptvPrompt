package com.backend.promptvprompt.DTO.Matchmaking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    private PlayerQueue playerOne;
    private PlayerQueue playerTwo;
}
