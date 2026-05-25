package com.bl21.blackjack.engine;

import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.enums.GameStatus;
import com.bl21.enums.Rank;
import com.bl21.enums.Suit;
import com.bl21.websocket.PrivateTable;
import com.bl21.websocket.TablePlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SplitFlowTests {

    @Test
    void soloModeSplitCreatesTwoHandsAndStandAdvancesAcrossThem() {
        BlackjackEngine game = new BlackjackEngine();

        Hand hand = game.getCurrentHand();
        hand.getCards().clear();
        hand.addCard(card(Rank.EIGHT));
        hand.addCard(card(Rank.EIGHT));

        game.getDealerHand().getCards().clear();
        game.getDealerHand().addCard(card(Rank.TEN));
        game.getDealerHand().addCard(card(Rank.SEVEN));

        game.playerSplit(0);

        assertEquals(2, game.getPlayerHands().size());
        assertEquals(2, game.getPlayerHands().get(0).getCards().size());
        assertEquals(2, game.getPlayerHands().get(1).getCards().size());
        assertEquals(0, game.getCurrentHandIndex());

        game.playerStand();

        assertEquals(1, game.getCurrentHandIndex());
        assertEquals(GameStatus.PLAYER_TURN, game.getGameStatus());

        game.playerStand();

        assertEquals(GameStatus.FINISHED, game.getGameStatus());
    }

    @Test
    void privateTableSplitAdvancesHandsBeforeNextPlayer() {
        PrivateTable table = new PrivateTable("table-1", "maurix", 1000L);
        TablePlayer player = table.getPlayers().get(0);

        player.placeBet(100L);
        player.setReady(true);
        player.prepareRoundHand();
        player.getHand().addCard(card(Rank.EIGHT));
        player.getHand().addCard(card(Rank.EIGHT));

        player.splitHand(table.getShoe());

        assertEquals(2, player.getHands().size());
        assertEquals(800L, player.getStack());
        assertEquals(200L, player.getCurrentBet());

        table.nextTurn();

        assertEquals(0, table.getCurrentPlayerTurn());
        assertEquals(1, player.getCurrentHandIndex());

        table.nextTurn();

        assertEquals(1, table.getCurrentPlayerTurn());
        assertFalse(table.getCurrentPlayerTurn() < table.getActiveRoundPlayers().size());
    }

    @Test
    void privateTableSplitSettlesEachHandAndClearsBets() {
        PrivateTable table = new PrivateTable("table-1", "maurix", 1000L);
        TablePlayer player = table.getPlayers().get(0);

        player.placeBet(100L);
        player.setReady(true);
        player.prepareRoundHand();
        player.getHand().addCard(card(Rank.EIGHT));
        player.getHand().addCard(card(Rank.EIGHT));
        player.splitHand(table.getShoe());

        replaceHand(player.getHands().get(0), Rank.TEN, Rank.NINE);
        replaceHand(player.getHands().get(1), Rank.TEN, Rank.SEVEN);
        replaceHand(table.getDealerHand(), Rank.TEN, Rank.EIGHT);

        table.resolveRound();

        assertEquals("WIN / LOSE", player.getRoundResult());
        assertEquals(1000L, player.getStack());
        assertEquals(0L, player.getCurrentBet());
        assertEquals(0L, player.getHandBet(0));
        assertEquals(0L, player.getHandBet(1));
    }

    @Test
    void privateTableTwentyOneAfterSplitPaysAsNormalWin() {
        PrivateTable table = new PrivateTable("table-1", "maurix", 1000L);
        TablePlayer player = table.getPlayers().get(0);

        player.placeBet(100L);
        player.setReady(true);
        player.prepareRoundHand();
        player.getHand().addCard(card(Rank.KING));
        player.getHand().addCard(card(Rank.KING));
        player.splitHand(table.getShoe());

        replaceHand(player.getHands().get(0), Rank.ACE, Rank.KING);
        replaceHand(player.getHands().get(1), Rank.TEN, Rank.SEVEN);
        replaceHand(table.getDealerHand(), Rank.TEN, Rank.NINE);

        table.resolveRound();

        assertEquals("WIN / LOSE", player.getRoundResult());
        assertEquals(1000L, player.getStack());
    }

    @Test
    void privateTableDealerDoesNotDrawWhenEveryPlayerBusts() {
        PrivateTable table = new PrivateTable("table-1", "maurix", 1000L);
        TablePlayer player = table.getPlayers().get(0);

        player.placeBet(100L);
        player.setReady(true);
        player.prepareRoundHand();
        player.getHand().addCard(card(Rank.KING));
        player.getHand().addCard(card(Rank.QUEEN));
        player.getHand().addCard(card(Rank.TWO));
        replaceHand(table.getDealerHand(), Rank.FIVE, Rank.SIX);

        int remainingCards = table.getShoe().remainingCards();

        table.playDealerTurnIfNeeded();

        assertEquals(remainingCards, table.getShoe().remainingCards());
        assertEquals(2, table.getDealerHand().getCards().size());
    }

    @Test
    void privateTableTwentyOneHandIsSkippedAutomatically() {
        PrivateTable table = new PrivateTable("table-1", "maurix", 1000L);
        TablePlayer player = table.getPlayers().get(0);

        player.placeBet(100L);
        player.setReady(true);
        player.prepareRoundHand();
        player.getHand().markSplitHand();
        player.getHand().addCard(card(Rank.ACE));
        player.getHand().addCard(card(Rank.KING));

        player.skipAutomaticHands();

        assertFalse(player.hasPlayableHand());
    }

    @Test
    void privateTableSplitDoubleAndResolveKeepsPerHandFlow() {
        PrivateTable table = new PrivateTable("table-1", "maurix", 1000L);
        TablePlayer player = table.getPlayers().get(0);

        player.placeBet(200L);
        player.setReady(true);
        player.prepareRoundHand();
        player.getHand().addCard(card(Rank.EIGHT));
        player.getHand().addCard(card(Rank.EIGHT));

        player.splitHand(table.getShoe());

        assertEquals(2, player.getHands().size());
        assertEquals(600L, player.getStack());
        assertEquals(400L, player.getCurrentBet());
        assertEquals(200L, player.getHandBet(0));
        assertEquals(200L, player.getHandBet(1));

        replaceHand(player.getHands().get(0), Rank.EIGHT, Rank.TWO);
        player.doubleBet();
        player.getHand().addCard(card(Rank.NINE));
        table.nextTurn();

        assertEquals(1, player.getCurrentHandIndex());
        assertEquals(400L, player.getStack());
        assertEquals(600L, player.getCurrentBet());
        assertEquals(400L, player.getHandBet(0));

        replaceHand(player.getHands().get(1), Rank.EIGHT, Rank.THREE);
        table.nextTurn();
        replaceHand(table.getDealerHand(), Rank.TEN, Rank.SEVEN);
        table.resolveRound();

        assertEquals("WIN / LOSE", player.getRoundResult());
        assertEquals(1200L, player.getStack());
        assertEquals(0L, player.getCurrentBet());
        assertEquals(0L, player.getHandBet(0));
        assertEquals(0L, player.getHandBet(1));
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
