package com.bl21.websocket;
import com.bl21.blackjack.engine.DealerEngine;
import com.bl21.blackjack.deck.Shoe;
import com.bl21.entity.Hand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateTable {

    private String tableId;

    private String hostUsername;
    private Long buyIn;

    private List<TablePlayer> players =
            new ArrayList<>();
    private Shoe shoe = new Shoe(6);

    private Hand dealerHand = new Hand();



    private int currentPlayerTurn = 0;

    private DealerEngine dealerEngine =
            new DealerEngine();

    private boolean roundStarted = false;

    private long roundStartTime;

    private boolean countdownStarted = false;

    private Map<String, Long> lastRoundCoinsChanges =
            new HashMap<>();

    private boolean historySavedForRound = true;

    public PrivateTable(
            String tableId,
            String hostUsername,
            Long buyIn
    ) {

        this.tableId = tableId;

        this.hostUsername = hostUsername;

        this.buyIn = buyIn;

        players.add(
                new TablePlayer(
                        hostUsername,
                        buyIn
                )
        );


    }
    public void playDealerTurn() {

        dealerEngine.playDealerHand(
                dealerHand,
                shoe
        );
    }

    public boolean isCountdownStarted() {
        return countdownStarted;
    }

    public long getCountdownRemainingMs() {
        if (!countdownStarted) {
            return 0L;
        }

        long elapsed =
                System.currentTimeMillis() - roundStartTime;

        return Math.max(0L, 10000L - elapsed);
    }
    public boolean isRoundStarted() {
        return roundStarted;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public Shoe getShoe() {
        return shoe;
    }

    public Hand getDealerHand() {
        return dealerHand;
    }
    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public String getTableId() {
        return tableId;
    }
    public Long getBuyIn() {
        return buyIn;
    }

    public String getHostUsername() {
        return hostUsername;
    }

    public List<TablePlayer>  getPlayers(){
        return players;
    }
    public boolean allPlayersFinished() {
        return currentPlayerTurn >= getActiveRoundPlayers().size();
    }


    public void removePlayer(
            String username
    ) {

        players.removeIf(
                player ->
                        player.getUsername()
                                .equals(username)
        );
    }

    public void nextTurn() {

        TablePlayer player = getCurrentPlayer();

        if (!player.advanceHand()) {
            currentPlayerTurn++;
        }

        skipFinishedPlayers();
    }
    public TablePlayer getCurrentPlayer() {

        List<TablePlayer> activePlayers = getActiveRoundPlayers();

        if (activePlayers.isEmpty()) {
            throw new RuntimeException("No active players");
        }

        if (currentPlayerTurn >= activePlayers.size()) {
            throw new RuntimeException("No active player turn");
        }

        return activePlayers.get(currentPlayerTurn);
    }

    public void startRound() {

        shoe.shuffleIfCutReached();

        dealerHand = new Hand();

        lastRoundCoinsChanges.clear();

        historySavedForRound = false;

        dealerHand.addCard(
                shoe.drawCard()
        );

        dealerHand.addCard(
                shoe.drawCard()
        );

        for (TablePlayer player : getActiveRoundPlayers()) {

            player.clearRoundResult();

            player.prepareRoundHand();

            player.getHand()
                    .addCard(
                            shoe.drawCard()
                    );

            player.getHand()
                    .addCard(
                            shoe.drawCard()
                    );

            player.skipAutomaticHands();
        }

        currentPlayerTurn = 0;

        skipFinishedPlayers();
    }

    public void startRoundNow() {

        roundStarted = true;

        startRound();

        if (dealerHand.isBlackjack() || allPlayersFinished()) {
            resolveRound();
        }
    }

    public void resolveRound() {

        int dealerValue =
                dealerHand.calculateValue();

        boolean dealerBust =
                dealerHand.isBust();

        for (TablePlayer player : getActiveRoundPlayers()) {

            long stackBeforeRound =
                    player.getStack() + player.getCurrentBet();

            List<String> results = new ArrayList<>();

            for (int index = 0; index < player.getHands().size(); index++) {

                Hand hand = player.getHands().get(index);

                int playerValue =
                        hand.calculateValue();

                if (hand.isBlackjack() && dealerHand.isBlackjack()) {

                    results.add("PUSH");
                    pushPlayerHand(player, index);
                    continue;
                }

                if (hand.isBlackjack()) {

                    results.add("BLACKJACK");
                    settlePlayerHand(player, index, 1.5);
                    continue;
                }

                if (dealerHand.isBlackjack()) {

                    results.add("LOSE");
                    continue;
                }

                if (hand.isBust()) {

                    results.add("BUST");
                    continue;
                }

                if (dealerBust) {

                    results.add("WIN");
                    settlePlayerHand(player, index, 1.0);
                    continue;
                }

                if (playerValue > dealerValue) {

                    results.add("WIN");
                    settlePlayerHand(player, index, 1.0);

                } else if (playerValue == dealerValue) {

                    results.add("PUSH");
                    pushPlayerHand(player, index);

                } else {

                    results.add("LOSE");
                }
            }

            player.setRoundResult(String.join(" / ", results));

            lastRoundCoinsChanges.put(
                    player.getUsername(),
                    player.getStack() - stackBeforeRound
            );

            player.clearSettledBets();
        }

        currentPlayerTurn = 0;
        roundStarted = false;
        countdownStarted = false;
    }

    private void settlePlayerHand(TablePlayer player, int handIndex, double multiplier) {

        long handBet = player.getHandBet(handIndex);
        long winnings = (long) (handBet * multiplier);

        player.setStack(player.getStack() + handBet + winnings);
    }

    private void pushPlayerHand(TablePlayer player, int handIndex) {

        player.setStack(player.getStack() + player.getHandBet(handIndex));
    }
    public List<TablePlayer> getReadyPlayers() {

        return players.stream()

                .filter(TablePlayer::isReady)

                .toList();
    }

    public boolean canStartCountdown() {

        return !roundStarted
                && !getActiveRoundPlayers().isEmpty();
    }

    public void startCountdown() {

        countdownStarted = true;

        roundStarted = false;

        roundStartTime =
                System.currentTimeMillis();
    }

    public boolean countdownFinished() {

        return System.currentTimeMillis()
                - roundStartTime >= 10000;
    }

    public void autoStartRound() {

        if (
                countdownStarted
                        && !roundStarted
                        && countdownFinished()
        ) {

            countdownStarted = false;

            roundStarted = true;

            startRound();

            if (dealerHand.isBlackjack() || allPlayersFinished()) {
                resolveRound();
            }
        }
    }
    public void resetRound() {

        currentPlayerTurn = 0;

        roundStarted = false;

        for (TablePlayer player : players) {

            player.resetForNextRound();
        }
    }

    public boolean hasFinishedRound() {
        return players.stream()
                .anyMatch(player -> player.getRoundResult() != null);
    }

    public void resetFinishedRoundIfNeeded() {
        if (hasFinishedRound()) {
            resetRound();
        }
    }

    public void updateCountdownState() {

        if (roundStarted) {
            return;
        }

        if (canStartCountdown()) {

            if (!countdownStarted) {
                startCountdown();
            }

            autoStartRound();

        } else {

            countdownStarted = false;
        }
    }

    public List<TablePlayer> getActiveRoundPlayers() {

        return players.stream()
                .filter(player ->
                        player.isReady()
                                && player.getCurrentBet() > 0
                )
                .toList();
    }

    public Long getLastRoundCoinsChange(String username) {
        return lastRoundCoinsChanges.get(username);
    }

    public Map<String, Long> getLastRoundCoinsChanges() {
        return Map.copyOf(lastRoundCoinsChanges);
    }

    public boolean hasUnsavedRoundHistory() {
        return !historySavedForRound
                && !lastRoundCoinsChanges.isEmpty();
    }

    public void markRoundHistorySaved() {
        historySavedForRound = true;
    }

    private void skipFinishedPlayers() {

        List<TablePlayer> activePlayers = getActiveRoundPlayers();

        while (currentPlayerTurn < activePlayers.size()
                && !activePlayers.get(currentPlayerTurn).hasPlayableHand()) {

            currentPlayerTurn++;
        }
    }
}
