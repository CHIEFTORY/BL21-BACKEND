package com.bl21.dto.response;

public class TrainerMoveResponse {

    private boolean correct;

    private String playerMove;

    private String correctMove;

    public TrainerMoveResponse(
            boolean correct,
            String playerMove,
            String correctMove
    ) {

        this.correct = correct;
        this.playerMove = playerMove;
        this.correctMove = correctMove;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getPlayerMove() {
        return playerMove;
    }

    public String getCorrectMove() {
        return correctMove;
    }
}