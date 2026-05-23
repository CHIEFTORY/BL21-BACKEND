package com.bl21.controller;

import com.bl21.blackjack.engine.BlackjackEngine;
import com.bl21.dto.response.GameStateResponse;
import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.entity.User;
import com.bl21.enums.GameStatus;
import com.bl21.enums.Rank;
import com.bl21.enums.Suit;
import com.bl21.repository.UserRepository;
import com.bl21.service.GameManager;
import com.bl21.service.GameResultService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameControllerSplitTests {

    @Test
    void standAfterSplitReturnsNextActiveHandInsteadOfSettlingGame() throws Exception {
        GameManager gameManager = new GameManager();
        UserRepository userRepository = mock(UserRepository.class);
        GameResultService resultService = mock(GameResultService.class);
        GameController controller = new GameController(gameManager, resultService, userRepository);
        User user = new User();
        user.setUsername("maurix");
        user.setCoins(10000L);

        when(userRepository.findByUsername("maurix")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("maurix", "password"));

        String gameId = gameManager.createGame(100L);
        BlackjackEngine game = gameManager.getGame(gameId);
        forcePair(game);
        game.playerSplit(0);
        replaceHand(game.getPlayerHands().get(0), Rank.EIGHT, Rank.TWO);
        replaceHand(game.getPlayerHands().get(1), Rank.EIGHT, Rank.THREE);

        GameStateResponse response = controller.stand(gameId);

        assertEquals("PLAYER_TURN", response.getStatus());
        assertEquals(1, response.getCurrentHandIndex());
        assertEquals(GameStatus.PLAYER_TURN, gameManager.getGame(gameId).getGameStatus());
    }

    @Test
    void splitCreatesEqualBetForEachHand() throws Exception {
        GameManager gameManager = new GameManager();
        UserRepository userRepository = mock(UserRepository.class);
        GameResultService resultService = mock(GameResultService.class);
        GameController controller = new GameController(gameManager, resultService, userRepository);
        User user = new User();
        user.setUsername("maurix");
        user.setCoins(1000L);

        when(userRepository.findByUsername("maurix")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("maurix", "password"));

        String gameId = gameManager.createGame(10L);
        BlackjackEngine game = gameManager.getGame(gameId);
        forcePair(game);

        GameStateResponse response = controller.split(gameId);

        assertEquals(20L, response.getBet());
        assertEquals(10L, gameManager.getHandBet(gameId, 0));
        assertEquals(10L, gameManager.getHandBet(gameId, 1));
    }

    @Test
    void splitAcesUsesOriginalHandIndexForBet() throws Exception {
        GameManager gameManager = new GameManager();
        UserRepository userRepository = mock(UserRepository.class);
        GameResultService resultService = mock(GameResultService.class);
        GameController controller = new GameController(gameManager, resultService, userRepository);
        User user = new User();
        user.setUsername("maurix");
        user.setCoins(1000L);

        when(userRepository.findByUsername("maurix")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("maurix", "password"));

        String gameId = gameManager.createTrainerGame("maurix", "pairs");
        BlackjackEngine game = gameManager.getGame(gameId);
        forceAces(game);

        GameStateResponse response = controller.split(gameId);

        assertEquals(2, response.getPlayerHands().size());
        assertEquals(100L, response.getHandBets().get(0));
        assertEquals(100L, response.getHandBets().get(1));
    }

    @Test
    void splitRequiresEnoughCoinsForBothHands() {
        GameManager gameManager = new GameManager();
        UserRepository userRepository = mock(UserRepository.class);
        GameResultService resultService = mock(GameResultService.class);
        GameController controller = new GameController(gameManager, resultService, userRepository);
        User user = new User();
        user.setUsername("maurix");
        user.setCoins(15L);

        when(userRepository.findByUsername("maurix")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("maurix", "password"));

        String gameId = gameManager.createGame(10L);
        BlackjackEngine game = gameManager.getGame(gameId);
        forcePair(game);

        assertThrows(RuntimeException.class, () -> controller.split(gameId));
    }

    private void forcePair(BlackjackEngine game) {
        Hand hand = game.getCurrentHand();
        hand.getCards().clear();
        hand.addCard(card(Rank.EIGHT));
        hand.addCard(card(Rank.EIGHT));

        game.getDealerHand().getCards().clear();
        game.getDealerHand().addCard(card(Rank.TEN));
        game.getDealerHand().addCard(card(Rank.SEVEN));
    }

    private void forceAces(BlackjackEngine game) {
        Hand hand = game.getCurrentHand();
        hand.getCards().clear();
        hand.addCard(card(Rank.ACE));
        hand.addCard(card(Rank.ACE));

        game.getDealerHand().getCards().clear();
        game.getDealerHand().addCard(card(Rank.TEN));
        game.getDealerHand().addCard(card(Rank.SEVEN));
    }

    private void replaceHand(Hand hand, Rank first, Rank second) {
        hand.getCards().clear();
        hand.addCard(card(first));
        hand.addCard(card(second));
    }

    private Card card(Rank rank) {
        return new Card(Suit.SPADES, rank);
    }
}
