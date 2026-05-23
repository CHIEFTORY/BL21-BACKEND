package com.bl21.blackjack.deck;

import com.bl21.entity.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shoe {

    private List<Card> cards;
    private final int numberOfDecks;
    private final int totalCards;
    private final int cutCardRemaining;
    private boolean shuffledForNextRound = false;

    public Shoe(int numberOfDecks) {
        this.numberOfDecks = numberOfDecks;
        this.totalCards = numberOfDecks * 52;
        this.cutCardRemaining = Math.round(totalCards * 0.25f);

        cards = new ArrayList<>();

        buildShoe();

        shuffle();
    }

    private void buildShoe() {
        cards.clear();

        for (int i = 0; i < numberOfDecks; i++) {

            Deck deck = new Deck();

            cards.addAll(deck.getCards());
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public boolean shuffleIfCutReached() {
        if (!isCutCardReached()) {
            shuffledForNextRound = false;
            return false;
        }

        buildShoe();
        shuffle();
        shuffledForNextRound = true;
        return true;
    }

    public Card drawCard() {

        if (cards.isEmpty()) {
            throw new RuntimeException("Shoe is empty");
        }

        return cards.remove(0);
    }

    public int remainingCards() {
        return cards.size();
    }

    public int totalCards() {
        return totalCards;
    }

    public int usedCards() {
        return totalCards - cards.size();
    }

    public int penetrationPercent() {
        return Math.round((usedCards() * 100f) / totalCards);
    }

    public int cutCardRemaining() {
        return cutCardRemaining;
    }

    public boolean isCutCardReached() {
        return remainingCards() <= cutCardRemaining;
    }

    public boolean wasShuffledForNextRound() {
        return shuffledForNextRound;
    }

    public int getNumberOfDecks() {
        return numberOfDecks;
    }
}
