package com.bl21.dto.response;

public class ShoeStateResponse {

    private Integer decks;
    private Integer totalCards;
    private Integer remainingCards;
    private Integer usedCards;
    private Integer penetration;
    private Integer cutCardRemaining;
    private Boolean cutCardReached;
    private Boolean shuffled;

    public ShoeStateResponse(
            Integer decks,
            Integer totalCards,
            Integer remainingCards,
            Integer usedCards,
            Integer penetration,
            Integer cutCardRemaining,
            Boolean cutCardReached,
            Boolean shuffled
    ) {
        this.decks = decks;
        this.totalCards = totalCards;
        this.remainingCards = remainingCards;
        this.usedCards = usedCards;
        this.penetration = penetration;
        this.cutCardRemaining = cutCardRemaining;
        this.cutCardReached = cutCardReached;
        this.shuffled = shuffled;
    }

    public Integer getDecks() {
        return decks;
    }

    public Integer getTotalCards() {
        return totalCards;
    }

    public Integer getRemainingCards() {
        return remainingCards;
    }

    public Integer getUsedCards() {
        return usedCards;
    }

    public Integer getPenetration() {
        return penetration;
    }

    public Integer getCutCardRemaining() {
        return cutCardRemaining;
    }

    public Boolean getCutCardReached() {
        return cutCardReached;
    }

    public Boolean getShuffled() {
        return shuffled;
    }
}
