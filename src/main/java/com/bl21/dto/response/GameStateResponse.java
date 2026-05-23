package com.bl21.dto.response;

import java.util.List;

public class GameStateResponse {

    private String gameId;

    private List<HandResponse> playerHands;

    private HandResponse dealerHand;

    private String status;

    private Long bet;

    private String result;

    private Long coins;

    private Long coinsChange;

    private Integer currentHandIndex;

    private List<Long> handBets;

    private ShoeStateResponse shoe;

    public GameStateResponse(
            String gameId,
            List<HandResponse> playerHands,
            HandResponse dealerHand,
            String status
    ) {

        this.gameId = gameId;
        this.playerHands = playerHands;
        this.dealerHand = dealerHand;
        this.status = status;
        this.currentHandIndex = 0;
    }

    public GameStateResponse(
            String gameId,
            List<HandResponse> playerHands,
            HandResponse dealerHand,
            String status,
            Long bet,
            String result,
            Long coins,
            Long coinsChange,
            Integer currentHandIndex,
            List<Long> handBets,
            ShoeStateResponse shoe
    ) {

        this.gameId = gameId;
        this.playerHands = playerHands;
        this.dealerHand = dealerHand;
        this.status = status;
        this.bet = bet;
        this.result = result;
        this.coins = coins;
        this.coinsChange = coinsChange;
        this.currentHandIndex = currentHandIndex;
        this.handBets = handBets;
        this.shoe = shoe;
    }

    public String getGameId() {
        return gameId;
    }

    public List<HandResponse> getPlayerHands() {
        return playerHands;
    }

    public HandResponse getDealerHand() {
        return dealerHand;
    }

    public String getStatus() {
        return status;
    }

    public Long getBet() {
        return bet;
    }

    public String getResult() {
        return result;
    }

    public Long getCoins() {
        return coins;
    }

    public Long getCoinsChange() {
        return coinsChange;
    }

    public Integer getCurrentHandIndex() {
        return currentHandIndex;
    }

    public List<Long> getHandBets() {
        return handBets;
    }

    public ShoeStateResponse getShoe() {
        return shoe;
    }
}
