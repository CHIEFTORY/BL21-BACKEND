package com.bl21.blackjack.deck;

import com.bl21.entity.Card;
import com.bl21.enums.Rank;
import com.bl21.enums.Suit;

import java.util.ArrayList;
import java.util.List;

public class Deck {

    private List<Card> cards;

    public Deck() {

        cards = new ArrayList<>();

        for (Suit suit : Suit.values()) {

            for (Rank rank : Rank.values()) {

                cards.add(new Card(suit, rank));
            }
        }
    }

    public List<Card> getCards() {
        return cards;
    }
}