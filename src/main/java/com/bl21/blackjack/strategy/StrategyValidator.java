package com.bl21.blackjack.strategy;

import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.enums.MoveAction;

public class StrategyValidator {

    private BasicStrategyEngine strategyEngine;

    public StrategyValidator() {

        strategyEngine = new BasicStrategyEngine();
    }

    public boolean isCorrectMove(
            Hand playerHand,
            Card dealerCard,
            MoveAction playerMove
    ) {

        MoveAction bestMove =
                getCorrectMove(playerHand, dealerCard);

        return bestMove == playerMove;
    }

    public MoveAction getCorrectMove(
            Hand playerHand,
            Card dealerCard
    ) {

        MoveAction bestMove = strategyEngine.getBestMove(playerHand, dealerCard);

        if (bestMove == MoveAction.DOUBLE && !playerHand.canDouble()) {
            return MoveAction.HIT;
        }

        if (bestMove == MoveAction.SPLIT && !playerHand.canSplit()) {
            return MoveAction.HIT;
        }

        return bestMove;
    }
}
