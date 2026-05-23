package com.bl21.service;

import com.bl21.blackjack.engine.BlackjackEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameManagerTrainerModeTests {

    @Test
    void createsHardTrainerHands() {
        GameManager manager = new GameManager();

        BlackjackEngine game = manager.getGame(manager.createTrainerGame("hard"));

        assertFalse(game.getCurrentHand().isSoft());
        assertFalse(game.getCurrentHand().canSplit());
    }

    @Test
    void createsSoftTrainerHands() {
        GameManager manager = new GameManager();

        BlackjackEngine game = manager.getGame(manager.createTrainerGame("soft"));

        assertTrue(game.getCurrentHand().isSoft());
        assertFalse(game.getCurrentHand().canSplit());
    }

    @Test
    void createsPairTrainerHands() {
        GameManager manager = new GameManager();

        BlackjackEngine game = manager.getGame(manager.createTrainerGame("pairs"));

        assertTrue(game.getCurrentHand().canSplit());
    }

    @Test
    void trainerResponseUsesShoeWithInitialCardsConsumed() {
        GameManager manager = new GameManager();

        BlackjackEngine game = manager.getGame(manager.createTrainerGame("all"));

        assertTrue(game.getShoe().remainingCards() <= 308);
        assertTrue(game.getShoe().remainingCards() >= 306);
    }

    @Test
    void trainerKeepsSameUserShoeAcrossNewHands() {
        GameManager manager = new GameManager();

        BlackjackEngine firstGame =
                manager.getGame(manager.createTrainerGame("maurix", "all"));
        int remainingAfterFirstHand =
                firstGame.getShoe().remainingCards();

        BlackjackEngine secondGame =
                manager.getGame(manager.createTrainerGame("maurix", "all"));
        int remainingAfterSecondHand =
                secondGame.getShoe().remainingCards();

        assertEquals(308, remainingAfterFirstHand);
        assertEquals(304, remainingAfterSecondHand);
    }

    @Test
    void pairsTrainerHandExposesSplitAsLegalMove() {
        GameManager manager = new GameManager();

        BlackjackEngine game =
                manager.getGame(manager.createTrainerGame("maurix", "pairs"));

        assertTrue(game.getCurrentHand().canSplit());
    }
}
