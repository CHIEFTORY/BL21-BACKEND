package com.bl21.mapper;

import com.bl21.dto.response.CardResponse;
import com.bl21.dto.response.HandResponse;
import com.bl21.entity.Card;
import com.bl21.entity.Hand;

import java.util.ArrayList;
import java.util.List;

public class GameMapper {

    public static CardResponse toCardResponse(
            Card card
    ) {

        return new CardResponse(
                card.getSuit().name(),
                card.getRank().name(),
                card.getValue(),
                false
        );
    }

    public static CardResponse hiddenCard() {

        return new CardResponse(
                null,
                null,
                0,
                true
        );
    }

    public static HandResponse toHandResponse(
            Hand hand
    ) {

        List<CardResponse> cards =
                hand.getCards()
                        .stream()
                        .map(GameMapper::toCardResponse)
                        .toList();

        return new HandResponse(
                cards,
                hand.calculateValue(),
                hand.isBlackjack(),
                hand.isBust(),
                hand.isLocked(),
                hand.canSplit(),
                hand.canDouble()
        );
    }

    public static HandResponse toDealerHandResponse(
            Hand hand,
            boolean hideSecondCard
    ) {

        List<CardResponse> cards =
                new ArrayList<>();

        for (int i = 0; i < hand.getCards().size(); i++) {

            if (hideSecondCard && i == 1) {

                cards.add(hiddenCard());

            } else {

                cards.add(
                        toCardResponse(
                                hand.getCards().get(i)
                        )
                );
            }
        }

        return new HandResponse(
                cards,
                hideSecondCard
                        ? hand.getCards().get(0).getValue()
                        : hand.calculateValue(),
                false,
                false,
                false,
                false,
                false
        );
    }
}
