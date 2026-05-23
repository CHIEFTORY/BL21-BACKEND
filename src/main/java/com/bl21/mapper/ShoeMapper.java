package com.bl21.mapper;

import com.bl21.blackjack.deck.Shoe;
import com.bl21.dto.response.ShoeStateResponse;

public class ShoeMapper {

    private ShoeMapper() {
    }

    public static ShoeStateResponse toResponse(Shoe shoe) {
        return new ShoeStateResponse(
                shoe.getNumberOfDecks(),
                shoe.totalCards(),
                shoe.remainingCards(),
                shoe.usedCards(),
                shoe.penetrationPercent(),
                shoe.cutCardRemaining(),
                shoe.isCutCardReached(),
                shoe.wasShuffledForNextRound()
        );
    }
}
