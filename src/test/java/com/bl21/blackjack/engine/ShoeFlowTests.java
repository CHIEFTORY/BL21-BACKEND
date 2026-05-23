package com.bl21.blackjack.engine;

import com.bl21.blackjack.deck.Shoe;
import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.enums.Rank;
import com.bl21.enums.Suit;
import com.bl21.websocket.PrivateTable;
import com.bl21.websocket.TablePlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShoeFlowTests {

    @Test
    void soloInitialDealConsumesFourCardsAndHitConsumesOneMore() {
        Shoe shoe = new Shoe(6);
        BlackjackEngine game = new BlackjackEngine(shoe);

        assertEquals(312, shoe.remainingCards());

        Hand playerHand = new Hand();
        playerHand.addCard(card(Rank.FIVE));
        playerHand.addCard(card(Rank.SIX));

        Hand dealerHand = new Hand();
        dealerHand.addCard(card(Rank.TEN));
        dealerHand.addCard(card(Rank.SEVEN));

        game.startPresetGame(playerHand, dealerHand);

        assertEquals(308, shoe.remainingCards());

        game.playerHit(0);

        assertEquals(307, shoe.remainingCards());
    }

    @Test
    void splitConsumesOneNewCardForEachSplitHand() {
        Shoe shoe = new Shoe(6);
        BlackjackEngine game = new BlackjackEngine(shoe);

        game.startGame();
        forcePair(game);

        int beforeSplit = shoe.remainingCards();

        game.playerSplit(0);

        assertEquals(beforeSplit - 2, shoe.remainingCards());
    }

    @Test
    void trainerGameStartsWithFourCardsConsumed() {
        BlackjackEngine game = new BlackjackEngine();

        game.startGame();

        assertEquals(308, game.getShoe().remainingCards());
    }

    @Test
    void privateTableInitialRoundConsumesDealerAndPlayerCardsFromSharedShoe() {
        PrivateTable table = new PrivateTable("table-1", "maurix", 1000L);
        TablePlayer player = table.getPlayers().get(0);

        player.placeBet(100L);
        player.setReady(true);

        table.startRoundNow();

        assertEquals(308, table.getShoe().remainingCards());
    }

    @Test
    void privateTableHitConsumesOneCardFromSharedShoe() {
        PrivateTable table = new PrivateTable("table-1", "maurix", 1000L);
        TablePlayer player = table.getPlayers().get(0);

        player.placeBet(100L);
        player.setReady(true);
        player.prepareRoundHand();
        table.getDealerHand().addCard(table.getShoe().drawCard());
        table.getDealerHand().addCard(table.getShoe().drawCard());
        player.getHand().addCard(table.getShoe().drawCard());
        player.getHand().addCard(table.getShoe().drawCard());

        int beforeHit = table.getShoe().remainingCards();

        player.getHand()
                .addCard(table.getShoe().drawCard());

        assertEquals(beforeHit - 1, table.getShoe().remainingCards());
    }

    private void forcePlayableHand(BlackjackEngine game) {
        replaceHand(game.getCurrentHand(), Rank.FIVE, Rank.SIX);
        replaceHand(game.getDealerHand(), Rank.TEN, Rank.SEVEN);
    }

    private void forcePair(BlackjackEngine game) {
        replaceHand(game.getCurrentHand(), Rank.EIGHT, Rank.EIGHT);
        replaceHand(game.getDealerHand(), Rank.TEN, Rank.SEVEN);
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
