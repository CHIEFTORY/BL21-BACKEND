package com.bl21.dto.response;

public class TrainerMistakeResponse {

    private String playerHand;

    private String dealerCard;

    private String playerMove;

    private String correctMove;

    private long mistakes;

    public TrainerMistakeResponse(
            String playerHand,
            String dealerCard,
            String playerMove,
            String correctMove,
            long mistakes
    ) {
        this.playerHand = playerHand;
        this.dealerCard = dealerCard;
        this.playerMove = playerMove;
        this.correctMove = correctMove;
        this.mistakes = mistakes;
    }

    public String getPlayerHand() {
        return playerHand;
    }

    public String getDealerCard() {
        return dealerCard;
    }

    public String getPlayerMove() {
        return playerMove;
    }

    public String getCorrectMove() {
        return correctMove;
    }

    public long getMistakes() {
        return mistakes;
    }
}
