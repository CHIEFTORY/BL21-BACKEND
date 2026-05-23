package com.bl21.service;

import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.entity.User;
import com.bl21.enums.Rank;
import com.bl21.enums.Suit;
import com.bl21.repository.UserRepository;
import com.bl21.websocket.PrivateTable;
import com.bl21.websocket.TablePlayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PrivateTableHistoryServiceTests {

    @Test
    void savesResolvedPrivateRoundAsPrivateTableHistoryOnlyOnce() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        GameResultService gameResultService = mock(GameResultService.class);
        PrivateTableHistoryService service =
                new PrivateTableHistoryService(userRepository, gameResultService);
        User user = new User();
        user.setUsername("maurix");

        when(userRepository.findByUsername("maurix"))
                .thenReturn(Optional.of(user));

        PrivateTable table =
                new PrivateTable("private-1", "maurix", 1000L);
        TablePlayer player =
                table.getPlayers().get(0);

        player.placeBet(100L);
        player.setReady(true);
        player.prepareRoundHand();
        markHistoryUnsaved(table);
        replaceHand(player.getHand(), Rank.TEN, Rank.NINE);
        replaceHand(table.getDealerHand(), Rank.TEN, Rank.EIGHT);
        table.resolveRound();

        service.saveIfNeeded(table);
        service.saveIfNeeded(table);

        verify(gameResultService, times(1))
                .saveHistory(
                        user,
                        "TEN of SPADES, NINE of SPADES",
                        "TEN of SPADES, EIGHT of SPADES",
                        "WIN",
                        100L,
                        "PRIVATE_TABLE"
                );
        verifyNoMoreInteractions(gameResultService);
    }

    private void replaceHand(Hand hand, Rank first, Rank second) {
        hand.getCards().clear();
        hand.addCard(card(first));
        hand.addCard(card(second));
    }

    private Card card(Rank rank) {
        return new Card(Suit.SPADES, rank);
    }

    private void markHistoryUnsaved(PrivateTable table) throws Exception {
        Field field =
                PrivateTable.class.getDeclaredField("historySavedForRound");
        field.setAccessible(true);
        field.set(table, false);
    }
}
