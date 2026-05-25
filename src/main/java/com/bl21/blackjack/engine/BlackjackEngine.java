package com.bl21.blackjack.engine;
import com.bl21.enums.GameStatus;
import com.bl21.blackjack.deck.Shoe;
import com.bl21.entity.Hand;
import com.bl21.enums.Rank;

import java.util.ArrayList;
import java.util.List;

public class BlackjackEngine {

    private Shoe shoe;

    private List<Hand> playerHands;

    private Hand dealerHand;

    private DealerEngine dealerEngine;

    private int currentHandIndex;

    private GameStatus gameStatus;

    public BlackjackEngine() {

        this(new Shoe(6));
    }

    public BlackjackEngine(Shoe shoe) {

        this.shoe = shoe;

        playerHands = new ArrayList<>();

        playerHands.add(new Hand());

        dealerHand = new Hand();

        dealerEngine = new DealerEngine();

        currentHandIndex = 0;

        gameStatus = GameStatus.PLAYER_TURN;
    }

    public void startGame() {

        shoe.shuffleIfCutReached();

        playerHands.get(0).addCard(shoe.drawCard());
        dealerHand.addCard(shoe.drawCard());

        playerHands.get(0).addCard(shoe.drawCard());
        dealerHand.addCard(shoe.drawCard());

        if (playerHands.get(0).isBlackjack() || dealerHand.isBlackjack()) {
            gameStatus = GameStatus.FINISHED;
        }
    }

    public void startPresetGame(
            Hand playerHand,
            Hand dealerPresetHand
    ) {

        shoe.shuffleIfCutReached();

        for (int index = 0; index < 4; index++) {
            shoe.drawCard();
        }

        playerHands.clear();
        playerHands.add(copyHand(playerHand));

        dealerHand = copyHand(dealerPresetHand);

        currentHandIndex = 0;

        gameStatus = GameStatus.PLAYER_TURN;
    }

    public void playerHit(int handIndex) {

        if (gameStatus != GameStatus.PLAYER_TURN) {
            return;
        }

        Hand hand = playerHands.get(handIndex);

        if (hand.isLocked()) {
            nextHand();
            return;
        }

        hand.addCard(shoe.drawCard());

        if (hand.isBust()) {

            nextHand();
        }
    }

    public void playerStand() {

        nextHand();
    }

    public void playerDouble(int handIndex) {

        Hand hand = playerHands.get(handIndex);

        if (!hand.canDouble()) {

            throw new RuntimeException("Cannot double after hit");
        }

        hand.addCard(shoe.drawCard());

        nextHand();
    }

    public String resolveGame(int handIndex) {

        Hand playerHand = playerHands.get(handIndex);

        int playerValue = playerHand.calculateValue();

        int dealerValue = dealerHand.calculateValue();

        if (playerHand.isBlackjack() && dealerHand.isBlackjack()) {
            return "PUSH";
        }

        if (playerHand.isBlackjack()) {
            return "BLACKJACK";
        }

        if (dealerHand.isBlackjack()) {
            return "DEALER WINS";
        }

        if (playerHand.isBust()) {
            return "PLAYER BUSTS - DEALER WINS";
        }

        if (dealerHand.isBust()) {
            return "DEALER BUSTS - PLAYER WINS";
        }

        if (playerValue > dealerValue) {
            return "PLAYER WINS";
        }

        if (dealerValue > playerValue) {
            return "DEALER WINS";
        }

        return "PUSH";
    }
    public void playerSplit(int handIndex) {

        Hand originalHand = playerHands.get(handIndex);
        if (playerHands.size() >= 4) {
            throw new RuntimeException("Maximum splits reached");
        }
        if (!originalHand.canSplit()) {
            throw new RuntimeException("Cannot split this hand");
        }

        Hand newHand = new Hand();
        boolean splitAces = originalHand.getCards().get(0).getRank() == Rank.ACE;

        newHand.addCard(originalHand.getCards().remove(1));

        originalHand.markSplitHand();
        newHand.markSplitHand();

        originalHand.addCard(shoe.drawCard());

        newHand.addCard(shoe.drawCard());

        if (splitAces) {
            originalHand.markSplitAces();
            newHand.markSplitAces();
        }

        playerHands.add(currentHandIndex + 1, newHand);

        skipLockedHands();

        if (currentHandIndex >= playerHands.size()) {
            finishDealerTurn();
        }
    }

    public List<Hand> getPlayerHands() {
        return playerHands;
    }

    public Hand getDealerHand() {
        return dealerHand;
    }

    public Shoe getShoe() {
        return shoe;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public Hand getCurrentHand() {

        return playerHands.get(currentHandIndex);
    }

    public void nextHand() {

        currentHandIndex++;

        skipLockedHands();

        if (currentHandIndex < playerHands.size()) {

            return;
        }

        finishDealerTurn();
    }

    private void skipLockedHands() {

        while (currentHandIndex < playerHands.size()
                && playerHands.get(currentHandIndex).isLocked()) {

            currentHandIndex++;
        }
    }

    private void finishDealerTurn() {

        gameStatus = GameStatus.DEALER_TURN;

        if (dealerShouldPlay()) {
            dealerEngine.playDealerHand(dealerHand, shoe);
        }

        gameStatus = GameStatus.FINISHED;
    }

    private boolean dealerShouldPlay() {
        return playerHands.stream()
                .anyMatch(hand -> !hand.isBust() && !hand.isBlackjack());
    }

    public int getCurrentHandIndex() {
        return currentHandIndex;
    }

    private Hand copyHand(Hand source) {
        Hand copy = new Hand();

        source.getCards()
                .forEach(copy::addCard);

        if (source.isSplitAces()) {
            copy.markSplitAces();
        } else if (source.isSplitHand()) {
            copy.markSplitHand();
        }

        return copy;
    }
}
