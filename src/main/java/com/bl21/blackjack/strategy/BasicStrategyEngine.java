package com.bl21.blackjack.strategy;

import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.enums.MoveAction;

public class BasicStrategyEngine {

    public MoveAction getBestMove(Hand playerHand, Card dealerCard) {
        return BasicStrategyTable.getMove(playerHand, dealerCard);
    }
}
