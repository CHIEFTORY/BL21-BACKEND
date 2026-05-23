package com.bl21.service;

import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.entity.User;
import com.bl21.repository.UserRepository;
import com.bl21.websocket.PrivateTable;
import com.bl21.websocket.TablePlayer;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class PrivateTableHistoryService {

    private final UserRepository userRepository;
    private final GameResultService gameResultService;

    public PrivateTableHistoryService(
            UserRepository userRepository,
            GameResultService gameResultService
    ) {
        this.userRepository = userRepository;
        this.gameResultService = gameResultService;
    }

    public void saveIfNeeded(PrivateTable table) {
        if (!table.hasUnsavedRoundHistory()) {
            return;
        }

        String dealerHand =
                formatHand(table.getDealerHand());

        for (TablePlayer player : table.getPlayers()) {
            Long coinsChange =
                    table.getLastRoundCoinsChange(player.getUsername());

            if (coinsChange == null || player.getRoundResult() == null) {
                continue;
            }

            User user =
                    userRepository
                            .findByUsername(player.getUsername())
                            .orElse(null);

            if (user == null) {
                continue;
            }

            gameResultService.saveHistory(
                    user,
                    player.getHands()
                            .stream()
                            .map(this::formatHand)
                            .collect(Collectors.joining(" | ")),
                    dealerHand,
                    player.getRoundResult(),
                    coinsChange,
                    "PRIVATE_TABLE"
            );
        }

        table.markRoundHistorySaved();
    }

    private String formatHand(Hand hand) {
        return hand.getCards()
                .stream()
                .map(Card::toString)
                .collect(Collectors.joining(", "));
    }
}
