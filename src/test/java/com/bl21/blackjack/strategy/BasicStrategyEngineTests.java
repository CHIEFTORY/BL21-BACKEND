package com.bl21.blackjack.strategy;

import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.enums.MoveAction;
import com.bl21.enums.Rank;
import com.bl21.enums.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicStrategyEngineTests {

    private final BasicStrategyEngine strategyEngine = new BasicStrategyEngine();
    private final StrategyValidator strategyValidator = new StrategyValidator();

    @Test
    void splitsAcesAndEightsAgainstAnyDealerCard() {
        assertMove(hand(Rank.ACE, Rank.ACE), dealer(Rank.TEN), MoveAction.SPLIT);
        assertMove(hand(Rank.EIGHT, Rank.EIGHT), dealer(Rank.ACE), MoveAction.SPLIT);
    }

    @Test
    void handlesNinePairExceptions() {
        assertMove(hand(Rank.NINE, Rank.NINE), dealer(Rank.NINE), MoveAction.SPLIT);
        assertMove(hand(Rank.NINE, Rank.NINE), dealer(Rank.SEVEN), MoveAction.STAND);
        assertMove(hand(Rank.NINE, Rank.NINE), dealer(Rank.ACE), MoveAction.STAND);
    }

    @Test
    void treatsFivePairAsHardTen() {
        assertMove(hand(Rank.FIVE, Rank.FIVE), dealer(Rank.NINE), MoveAction.DOUBLE);
        assertMove(hand(Rank.FIVE, Rank.FIVE), dealer(Rank.TEN), MoveAction.HIT);
    }

    @Test
    void completesSmallPairStrategy() {
        assertMove(hand(Rank.TWO, Rank.TWO), dealer(Rank.SEVEN), MoveAction.SPLIT);
        assertMove(hand(Rank.THREE, Rank.THREE), dealer(Rank.EIGHT), MoveAction.HIT);
        assertMove(hand(Rank.FOUR, Rank.FOUR), dealer(Rank.FIVE), MoveAction.SPLIT);
    }

    @Test
    void completesSoftTotals() {
        assertMove(hand(Rank.ACE, Rank.TWO), dealer(Rank.FIVE), MoveAction.DOUBLE);
        assertMove(hand(Rank.ACE, Rank.SEVEN), dealer(Rank.SIX), MoveAction.DOUBLE);
        assertMove(hand(Rank.ACE, Rank.SEVEN), dealer(Rank.NINE), MoveAction.HIT);
        assertMove(hand(Rank.ACE, Rank.EIGHT), dealer(Rank.ACE), MoveAction.STAND);
    }

    @Test
    void usesStandSoftSeventeenRulesForHardElevenAgainstAce() {
        assertMove(hand(Rank.SIX, Rank.FIVE), dealer(Rank.TEN), MoveAction.DOUBLE);
        assertMove(hand(Rank.SIX, Rank.FIVE), dealer(Rank.ACE), MoveAction.HIT);
    }

    @Test
    void downgradesDoubleRecommendationAfterInitialDecision() {
        Hand hardTenAfterHit = hand(Rank.TWO, Rank.FOUR);
        hardTenAfterHit.addCard(new Card(Suit.DIAMONDS, Rank.FOUR));

        assertEquals(MoveAction.HIT, strategyValidator.getCorrectMove(hardTenAfterHit, dealer(Rank.THREE)));
    }

    private void assertMove(Hand hand, Card dealerCard, MoveAction expected) {
        assertEquals(expected, strategyEngine.getBestMove(hand, dealerCard));
    }

    private Hand hand(Rank first, Rank second) {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.CLUBS, first));
        hand.addCard(new Card(Suit.HEARTS, second));

        return hand;
    }

    private Card dealer(Rank rank) {
        return new Card(Suit.SPADES, rank);
    }
}
