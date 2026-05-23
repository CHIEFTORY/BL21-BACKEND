package com.bl21;

import com.bl21.blackjack.deck.Deck;
import com.bl21.blackjack.deck.Shoe;
import com.bl21.blackjack.engine.BlackjackEngine;
import com.bl21.blackjack.engine.DealerEngine;
import com.bl21.blackjack.strategy.BasicStrategyEngine;
import com.bl21.blackjack.strategy.StrategyValidator;
import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.enums.MoveAction;
import com.bl21.enums.Rank;
import com.bl21.enums.Suit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Bl21Application {

    public static void main(String[] args) {

        SpringApplication.run(Bl21Application.class, args);
    }

}
