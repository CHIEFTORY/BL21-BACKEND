package com.bl21.mapper;

import com.bl21.dto.response.PrivateTableStateResponse;
import com.bl21.dto.response.TablePlayerResponse;
import com.bl21.entity.Hand;
import com.bl21.websocket.PrivateTable;
import com.bl21.websocket.TablePlayer;

import java.util.List;

public class PrivateTableMapper {

    public static PrivateTableStateResponse toStateResponse(
            PrivateTable table
    ) {

        if (table.isRoundStarted()) {
            table.normalizeTurn();
        }

        boolean roundFinished =
                table.getPlayers()
                        .stream()
                        .anyMatch(player ->
                                player.getRoundResult() != null
                        );

        String currentPlayerUsername = null;

        if (table.isRoundStarted()
                && !roundFinished
                && !table.allPlayersFinished()
                && !table.getPlayers().isEmpty()) {

            currentPlayerUsername =
                    table.getCurrentPlayer()
                            .getUsername();
        }

        Long countdownRemainingMs = null;

        if (!table.isRoundStarted()
                && table.isCountdownStarted()) {

            countdownRemainingMs =
                    table.getCountdownRemainingMs();
        }

        final String currentUsername = currentPlayerUsername;

        List<TablePlayerResponse> players =
                table.getPlayers()
                        .stream()
                        .map(player ->
                                toPlayerResponse(
                                        player,
                                        player.getUsername()
                                                .equals(currentUsername),
                                        table.getLastRoundCoinsChange(
                                                player.getUsername()
                                        )
                                )
                        )
                        .toList();

        String status = roundFinished
                ? "ROUND_FINISHED"
                : table.isRoundStarted()
                ? "PLAYER_TURN"
                : "WAITING_READY";

        boolean hideDealerSecondCard =
                table.isRoundStarted()
                        && !roundFinished;

        return new PrivateTableStateResponse(
                table.getTableId(),
                table.getHostUsername(),
                table.getBuyIn(),
                players,
                GameMapper.toDealerHandResponse(
                        table.getDealerHand(),
                        hideDealerSecondCard
                ),
                status,
                table.isRoundStarted(),
                countdownRemainingMs,
                currentPlayerUsername,
                ShoeMapper.toResponse(table.getShoe())
        );
    }

    private static TablePlayerResponse toPlayerResponse(
            TablePlayer player,
            boolean currentTurn,
            Long roundCoinsChange
    ) {

        return new TablePlayerResponse(
                player.getUsername(),
                player.getStack(),
                player.getCurrentBet(),
                player.isReady(),
                currentTurn,
                player.getRoundResult(),
                GameMapper.toHandResponse(
                        currentVisibleHand(player)
                ),
                player.getHands()
                        .stream()
                        .map(GameMapper::toHandResponse)
                        .toList(),
                player.getCurrentHandIndex(),
                player.getHandBets(),
                roundCoinsChange
        );
    }

    private static Hand currentVisibleHand(TablePlayer player) {
        if (player.getHands().isEmpty()) {
            return new Hand();
        }

        int index =
                Math.min(
                        player.getCurrentHandIndex(),
                        player.getHands().size() - 1
                );

        return player.getHands().get(index);
    }
}
