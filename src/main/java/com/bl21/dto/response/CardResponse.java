package com.bl21.dto.response;

public class CardResponse {

    private String suit;

    private String rank;

    private int value;

    private boolean hidden;

    public CardResponse(
            String suit,
            String rank,
            int value,
            boolean hidden
    ) {

        this.suit = suit;
        this.rank = rank;
        this.value = value;
        this.hidden = hidden;
    }

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    public int getValue() {
        return value;
    }

    public boolean isHidden() {
        return hidden;
    }
}