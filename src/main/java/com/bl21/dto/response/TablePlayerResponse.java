package com.bl21.dto.response;

import java.util.List;

public class TablePlayerResponse {

    private String username;

    private Long stack;

    private Long currentBet;

    private boolean ready;

    private boolean currentTurn;

    private String roundResult;

    private HandResponse hand;

    private List<HandResponse> hands;

    private int activeHandIndex;

    private List<Long> handBets;

    private Long roundCoinsChange;

    public TablePlayerResponse(
            String username,
            Long stack,
            Long currentBet,
            boolean ready,
            boolean currentTurn,
            String roundResult,
            HandResponse hand,
            List<HandResponse> hands,
            int activeHandIndex,
            List<Long> handBets,
            Long roundCoinsChange
    ) {

        this.username = username;
        this.stack = stack;
        this.currentBet = currentBet;
        this.ready = ready;
        this.currentTurn = currentTurn;
        this.roundResult = roundResult;
        this.hand = hand;
        this.hands = hands;
        this.activeHandIndex = activeHandIndex;
        this.handBets = handBets;
        this.roundCoinsChange = roundCoinsChange;
    }

    public String getUsername() {
        return username;
    }

    public Long getStack() {
        return stack;
    }

    public Long getCurrentBet() {
        return currentBet;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isCurrentTurn() {
        return currentTurn;
    }

    public String getRoundResult() {
        return roundResult;
    }

    public HandResponse getHand() {
        return hand;
    }

    public List<HandResponse> getHands() {
        return hands;
    }

    public int getActiveHandIndex() {
        return activeHandIndex;
    }

    public List<Long> getHandBets() {
        return handBets;
    }

    public Long getRoundCoinsChange() {
        return roundCoinsChange;
    }
}
