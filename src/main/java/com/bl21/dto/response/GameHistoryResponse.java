package com.bl21.dto.response;

import java.time.LocalDateTime;

public class GameHistoryResponse {

    private String playerHand;

    private String dealerHand;

    private String result;

    private Long coinsChange;

    private String mode;

    private LocalDateTime playedAt;

    public GameHistoryResponse(
            String playerHand,
            String dealerHand,
            String result,
            Long coinsChange,
            String mode,
            LocalDateTime playedAt
    ) {

        this.playerHand = playerHand;
        this.dealerHand = dealerHand;
        this.result = result;
        this.coinsChange = coinsChange;
        this.mode = mode;
        this.playedAt = playedAt;
    }

    public String getPlayerHand() {
        return playerHand;
    }

    public String getDealerHand() {
        return dealerHand;
    }

    public String getResult() {
        return result;
    }

    public Long getCoinsChange() {
        return coinsChange;
    }

    public String getMode() {
        return mode;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }
}
