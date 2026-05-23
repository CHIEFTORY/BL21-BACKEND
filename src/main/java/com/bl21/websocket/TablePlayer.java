package com.bl21.websocket;
import com.bl21.entity.Hand;
import com.bl21.enums.Rank;

import java.util.ArrayList;
import java.util.List;

public class TablePlayer {

    private String username;

    private Long stack;

    private Long currentBet = 0L;

    private List<Hand> hands = new ArrayList<>();

    private List<Long> handBets = new ArrayList<>();

    private int currentHandIndex = 0;

    private boolean ready = false;

    private String roundResult;

    public TablePlayer(
            String username,
            Long stack
    ) {

        if (stack == null || stack <= 0) {
            throw new RuntimeException("Invalid player stack");
        }

        this.username = username;
        this.stack = stack;
        this.hands.add(new Hand());
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public Hand getHand() {
        return hands.get(currentHandIndex);
    }

    public List<Hand> getHands() {
        return hands;
    }

    public int getCurrentHandIndex() {
        return currentHandIndex;
    }

    public boolean hasPlayableHand() {
        return currentHandIndex < hands.size();
    }
    public String getUsername() {
        return username;
    }

    public Long getStack() {
        return stack;
    }

    public void setStack(Long stack) {
        this.stack = stack;
    }

    public Long getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(Long currentBet) {
        this.currentBet = currentBet;
    }

    public String getRoundResult() {
        return roundResult;
    }

    public void setRoundResult(String roundResult) {
        this.roundResult = roundResult;
    }

    public void placeBet(Long amount) {

        if (amount > stack) {

            throw new RuntimeException(
                    "Not enough stack"
            );
        }

        stack -= amount;

        currentBet = amount;

        handBets.clear();

        handBets.add(amount);

        clearRoundResult();
    }

    public void winBet(double multiplier) {

        long winnings =
                (long) (getCurrentHandBet() * multiplier);

        stack += getCurrentHandBet() + winnings;

        clearCurrentHandBet();
    }
    public void loseBet() {

        clearCurrentHandBet();
    }

    public void pushBet() {

        stack += getCurrentHandBet();

        clearCurrentHandBet();
    }

    public void doubleBet() {

        Long handBet = getCurrentHandBet();

        if (handBet > stack) {

            throw new RuntimeException(
                    "Not enough stack to double"
            );
        }

        stack -= handBet;

        handBets.set(currentHandIndex, handBet * 2);

        currentBet += handBet;
    }

    public void splitHand(com.bl21.blackjack.deck.Shoe shoe) {

        Hand hand = getHand();

        if (hands.size() >= 4) {
            throw new RuntimeException("Maximum splits reached");
        }

        if (!hand.canSplit()) {
            throw new RuntimeException("Cannot split this hand");
        }

        Long handBet = getCurrentHandBet();

        if (handBet > stack) {
            throw new RuntimeException("Not enough stack to split");
        }

        stack -= handBet;

        currentBet += handBet;

        Hand newHand = new Hand();
        boolean splitAces = hand.getCards().get(0).getRank() == Rank.ACE;
        newHand.addCard(hand.getCards().remove(1));

        hand.addCard(shoe.drawCard());
        newHand.addCard(shoe.drawCard());

        if (splitAces) {
            hand.markSplitAces();
            newHand.markSplitAces();
        }

        hands.add(currentHandIndex + 1, newHand);
        handBets.add(currentHandIndex + 1, handBet);

        skipLockedHands();
    }

    public boolean advanceHand() {

        currentHandIndex++;

        skipLockedHands();

        return currentHandIndex < hands.size();
    }

    private void skipLockedHands() {

        while (currentHandIndex < hands.size()
                && (hands.get(currentHandIndex).isLocked()
                || hands.get(currentHandIndex).isBlackjack())) {

            currentHandIndex++;
        }
    }

    public void skipAutomaticHands() {

        skipLockedHands();
    }

    public void resetForNextRound() {

        ready = false;

        currentBet = 0L;

        clearRoundResult();

        hands.clear();

        hands.add(new Hand());

        handBets.clear();

        currentHandIndex = 0;
    }

    public void clearRoundResult() {

        roundResult = null;
    }

    public void prepareRoundHand() {

        hands.clear();

        hands.add(new Hand());

        currentHandIndex = 0;
    }

    public Long getHandBet(int index) {

        if (index >= handBets.size()) {
            return 0L;
        }

        return handBets.get(index);
    }

    public List<Long> getHandBets() {

        return List.copyOf(handBets);
    }

    public void clearSettledBets() {

        currentBet = 0L;

        for (int index = 0; index < handBets.size(); index++) {
            handBets.set(index, 0L);
        }
    }

    private Long getCurrentHandBet() {

        return getHandBet(currentHandIndex);
    }

    private void clearCurrentHandBet() {

        if (currentHandIndex < handBets.size()) {
            currentBet -= handBets.get(currentHandIndex);
            handBets.set(currentHandIndex, 0L);
        }
    }
}
