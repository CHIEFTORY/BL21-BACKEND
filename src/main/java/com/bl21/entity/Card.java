package com.bl21.entity;

import com.bl21.enums.Rank;
import com.bl21.enums.Suit;

public class Card {

    private Suit suit;
    private Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int getValue() {

        switch (rank) {

            case ACE:
                return 11;

            case TWO:
                return 2;

            case THREE:
                return 3;

            case FOUR:
                return 4;

            case FIVE:
                return 5;

            case SIX:
                return 6;

            case SEVEN:
                return 7;

            case EIGHT:
                return 8;

            case NINE:
                return 9;

            case TEN:
            case JACK:
            case QUEEN:
            case KING:
                return 10;

            default:
                return 0;
        }
    }

    public boolean isAce() {
        return rank == Rank.ACE;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}