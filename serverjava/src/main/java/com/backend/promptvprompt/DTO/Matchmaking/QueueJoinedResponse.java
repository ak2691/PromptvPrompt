package com.backend.promptvprompt.DTO.Matchmaking;

public class QueueJoinedResponse {
    private int position;

    public QueueJoinedResponse(int position) {
        this.position = position;
    }

    // Getters and setters
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
