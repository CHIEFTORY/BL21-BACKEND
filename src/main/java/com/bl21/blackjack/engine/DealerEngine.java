package com.bl21.blackjack.engine;

import com.bl21.blackjack.deck.Shoe;
import com.bl21.entity.Hand;

public class DealerEngine {

    public void playDealerHand(Hand dealerHand, Shoe shoe) {

        while (dealerHand.calculateValue() < 17) {

            dealerHand.addCard(shoe.drawCard());
        }
    }
}