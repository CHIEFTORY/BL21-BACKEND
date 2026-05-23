package com.bl21.entity;

import java.util.ArrayList;
import java.util.List;

public class Hand {

    private List<Card> cards;

    private boolean splitAces = false;

    public Hand() {
        this.cards = new ArrayList<>();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return cards;
    }

    public int calculateValue() {

        int total = 0;
        int aces = 0;

        for (Card card : cards) {

            total += card.getValue();

            if (card.isAce()) {
                aces++;
            }
        }

        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }

        return total;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && calculateValue() == 21 && !splitAces;
    }

    public boolean isBust() {
        return calculateValue() > 21;
    }

    public boolean isSoft() {

        int total = 0;
        int aces = 0;

        for (Card card : cards) {

            total += card.getValue();

            if (card.isAce()) {
                aces++;
            }
        }

        return aces > 0 && total <= 21;
    }

    @Override
    public String toString() {
        return cards.toString();
    }

    public boolean canSplit() {

        if (cards.size() != 2) {
            return false;
        }

        if (splitAces) {
            return false;
        }

        return cards.get(0).getRank() == cards.get(1).getRank();
    }

    public boolean canDouble() {

        return cards.size() == 2 && !splitAces;
    }

    public boolean isSplitAces() {
        return splitAces;
    }

    public void markSplitAces() {
        splitAces = true;
    }

    public boolean isLocked() {
        return splitAces;
    }
}
