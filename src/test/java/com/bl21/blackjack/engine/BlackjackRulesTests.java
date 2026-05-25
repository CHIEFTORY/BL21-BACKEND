package com.bl21.blackjack.engine;

import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.enums.GameStatus;
import com.bl21.enums.Rank;
import com.bl21.enums.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlackjackRulesTests {

    @Test
    void naturalBlackjackPaysAsBlackjackResult() {
        BlackjackEngine game = new BlackjackEngine();
        replaceHand(game.getCurrentHand(), Rank.ACE, Rank.KING);
        replaceHand(game.getDealerHand(), Rank.TEN, Rank.NINE);

        assertEquals("BLACKJACK", game.resolveGame(0));
    }

    @Test
    void dealerBlackjackBeatsNonBlackjackPlayer() {
        BlackjackEngine game = new BlackjackEngine();
        replaceHand(game.getCurrentHand(), Rank.TEN, Rank.NINE);
        replaceHand(game.getDealerHand(), Rank.ACE, Rank.QUEEN);

        assertEquals("DEALER WINS", game.resolveGame(0));
    }

    @Test
    void splitAcesReceiveOneCardAndFinishAutomatically() {
        BlackjackEngine game = new BlackjackEngine();
        replaceHand(game.getCurrentHand(), Rank.ACE, Rank.ACE);
        replaceHand(game.getDealerHand(), Rank.TEN, Rank.SEVEN);

        game.playerSplit(0);

        assertEquals(2, game.getPlayerHands().size());
        assertTrue(game.getPlayerHands().get(0).isLocked());
        assertTrue(game.getPlayerHands().get(1).isLocked());
        assertFalse(game.getPlayerHands().get(0).isBlackjack());
        assertEquals(GameStatus.FINISHED, game.getGameStatus());
    }

    @Test
    void twentyOneAfterSplitIsNotNaturalBlackjack() {
        BlackjackEngine game = new BlackjackEngine();
        replaceHand(game.getCurrentHand(), Rank.KING, Rank.KING);
        replaceHand(game.getDealerHand(), Rank.TEN, Rank.NINE);

        game.playerSplit(0);
        replaceHand(game.getPlayerHands().get(0), Rank.ACE, Rank.KING);

        assertFalse(game.getPlayerHands().get(0).isBlackjack());
        assertEquals("PLAYER WINS", game.resolveGame(0));
    }

    @Test
    void dealerDoesNotDrawWhenOnlySoloHandBusts() {
        BlackjackEngine game = new BlackjackEngine();
        replaceHand(game.getCurrentHand(), Rank.KING, Rank.QUEEN);
        replaceHand(game.getDealerHand(), Rank.FOUR, Rank.ACE);

        int remainingCards = game.getShoe().remainingCards();

        game.playerHit(0);

        assertEquals(GameStatus.FINISHED, game.getGameStatus());
        assertTrue(game.getPlayerHands().get(0).isBust());
        assertEquals(remainingCards - 1, game.getShoe().remainingCards());
        assertEquals(2, game.getDealerHand().getCards().size());
        assertEquals("PLAYER BUSTS - DEALER WINS", game.resolveGame(0));
    }

    private void replaceHand(Hand hand, Rank first, Rank second) {
        hand.getCards().clear();
        hand.addCard(card(first));
        hand.addCard(card(second));
    }

    private Card card(Rank rank) {
        return new Card(Suit.SPADES, rank);
    }
}
