package com.bl21.dto.response;

import java.util.List;

public class PrivateTableStateResponse {

    private String tableId;

    private String hostUsername;

    private Long buyIn;

    private List<TablePlayerResponse> players;

    private HandResponse dealerHand;

    private String status;

    private boolean roundStarted;

    private Long countdownRemainingMs;

    private String currentPlayerUsername;

    private ShoeStateResponse shoe;

    public PrivateTableStateResponse(
            String tableId,
            String hostUsername,
            Long buyIn,
            List<TablePlayerResponse> players,
            HandResponse dealerHand,
            String status,
            boolean roundStarted,
            Long countdownRemainingMs,
            String currentPlayerUsername,
            ShoeStateResponse shoe
    ) {

        this.tableId = tableId;
        this.hostUsername = hostUsername;
        this.buyIn = buyIn;
        this.players = players;
        this.dealerHand = dealerHand;
        this.status = status;
        this.roundStarted = roundStarted;
        this.countdownRemainingMs = countdownRemainingMs;
        this.currentPlayerUsername = currentPlayerUsername;
        this.shoe = shoe;
    }

    public String getTableId() {
        return tableId;
    }

    public String getHostUsername() {
        return hostUsername;
    }

    public Long getBuyIn() {
        return buyIn;
    }

    public List<TablePlayerResponse> getPlayers() {
        return players;
    }

    public HandResponse getDealerHand() {
        return dealerHand;
    }

    public String getStatus() {
        return status;
    }

    public boolean isRoundStarted() {
        return roundStarted;
    }

    public Long getCountdownRemainingMs() {
        return countdownRemainingMs;
    }

    public String getCurrentPlayerUsername() {
        return currentPlayerUsername;
    }

    public ShoeStateResponse getShoe() {
        return shoe;
    }
}
