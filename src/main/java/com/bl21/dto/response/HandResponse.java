package com.bl21.dto.response;

import java.util.List;

public class HandResponse {

    private List<CardResponse> cards;

    private int value;

    private boolean blackjack;

    private boolean bust;

    private boolean locked;

    private boolean canSplit;

    private boolean canDouble;

    public HandResponse(
            List<CardResponse> cards,
            int value,
            boolean blackjack,
            boolean bust,
            boolean locked,
            boolean canSplit,
            boolean canDouble
    ) {

        this.cards = cards;
        this.value = value;
        this.blackjack = blackjack;
        this.bust = bust;
        this.locked = locked;
        this.canSplit = canSplit;
        this.canDouble = canDouble;
    }

    public List<CardResponse> getCards() {
        return cards;
    }

    public int getValue() {
        return value;
    }

    public boolean isBlackjack() {
        return blackjack;
    }

    public boolean isBust() {
        return bust;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isCanSplit() {
        return canSplit;
    }

    public boolean isCanDouble() {
        return canDouble;
    }
}
