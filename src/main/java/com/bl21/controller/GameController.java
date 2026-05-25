package com.bl21.controller;
import com.bl21.dto.response.HandResponse;
import com.bl21.dto.request.StartGameRequest;
import com.bl21.mapper.GameMapper;
import com.bl21.mapper.ShoeMapper;
import com.bl21.blackjack.engine.BlackjackEngine;
import com.bl21.dto.response.GameStateResponse;
import com.bl21.service.GameManager;
import com.bl21.service.GameResultService;
import org.springframework.web.bind.annotation.*;
import com.bl21.entity.User;
import com.bl21.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameManager gameManager;
    private final GameResultService gameResultService;

    private final UserRepository userRepository;

    public GameController(
            GameManager gameManager,
            GameResultService gameResultService,
            UserRepository userRepository
    ) {

        this.gameManager = gameManager;
        this.gameResultService = gameResultService;
        this.userRepository = userRepository;
    }

    @PostMapping("/start")
    public GameStateResponse startGame(
            @RequestBody(required = false) StartGameRequest request
    ) {

        long bet = request != null && request.getBet() != null
                ? request.getBet()
                : 100L;

        if (bet <= 0) {

            throw new RuntimeException("Bet must be greater than zero");
        }

        User user = getCurrentUser();

        String activeGameId =
                gameManager.getActiveSoloGameId(
                        user.getUsername()
                );

        if (activeGameId != null) {
            BlackjackEngine activeGame =
                    gameManager.getGame(activeGameId);

            return toResponse(
                    activeGameId,
                    activeGame,
                    gameManager.getTotalExposure(activeGameId),
                    null,
                    user,
                    null
            );
        }

        if (user.getCoins() < bet) {

            throw new RuntimeException("Not enough coins");
        }

        String gameId = gameManager.createGame(user.getUsername(), bet);

        BlackjackEngine game = gameManager.getGame(gameId);

        if (game.getGameStatus().name().equals("FINISHED")) {
            return settleGame(gameId);
        }

        return toResponse(gameId, game, bet, null, user, null);
    }

    @PostMapping("/{gameId}/hit")
    public GameStateResponse hit(
            @PathVariable String gameId
    ) {

        BlackjackEngine game = gameManager.getGame(gameId);

        game.playerHit(game.getCurrentHandIndex());

        if (game.getGameStatus().name().equals("FINISHED")) {

            return settleGame(gameId);
        }

        User user = getCurrentUser();

        return toResponse(
                gameId,
                game,
                gameManager.getTotalExposure(gameId),
                null,
                user,
                null
        );
    }

    @PostMapping("/{gameId}/stand")
    public GameStateResponse stand(
            @PathVariable String gameId
    ) {

        BlackjackEngine game = gameManager.getGame(gameId);

        game.playerStand();

        if (game.getGameStatus().name().equals("FINISHED")) {
            return settleGame(gameId);
        }

        User user = getCurrentUser();

        return toResponse(
                gameId,
                game,
                gameManager.getTotalExposure(gameId),
                null,
                user,
                null
        );
    }

    @PostMapping("/{gameId}/double")
    public GameStateResponse doubleDown(
            @PathVariable String gameId
    ) {

        BlackjackEngine game = gameManager.getGame(gameId);

        int handIndex = game.getCurrentHandIndex();

        Long handBet =
                gameManager.getHandBet(gameId, handIndex);

        Long doubledExposure =
                gameManager.getTotalExposure(gameId) + handBet;

        User currentUser = getCurrentUser();

        if (!gameManager.isTrainerGame(gameId)
                && currentUser.getCoins() < doubledExposure) {

            throw new RuntimeException("Not enough coins");
        }

        game.playerDouble(handIndex);

        gameManager.doubleHandBet(gameId, handIndex);

        if (game.getGameStatus().name().equals("FINISHED")) {
            return settleGame(gameId);
        }

        return toResponse(
                gameId,
                game,
                gameManager.getTotalExposure(gameId),
                null,
                currentUser,
                null
        );
    }

    @PostMapping("/{gameId}/split")
    public GameStateResponse split(
            @PathVariable String gameId
    ) {

        BlackjackEngine game = gameManager.getGame(gameId);

        User user = getCurrentUser();

        int handIndex =
                game.getCurrentHandIndex();

        Long splitExposure =
                gameManager.getTotalExposure(gameId)
                        + gameManager.getHandBet(gameId, handIndex);

        if (!gameManager.isTrainerGame(gameId)
                && user.getCoins() < splitExposure) {

            throw new RuntimeException("Not enough coins to split");
        }

        game.playerSplit(handIndex);

        gameManager.splitHandBet(gameId, handIndex);

        if (game.getGameStatus().name().equals("FINISHED")) {
            return settleGame(gameId);
        }

        return toResponse(
                gameId,
                game,
                gameManager.getTotalExposure(gameId),
                null,
                user,
                null
        );
    }

    private GameStateResponse settleGame(String gameId) {

        BlackjackEngine game = gameManager.getGame(gameId);

        User user = getCurrentUser();

        long coinsBefore = user.getCoins();

        StringBuilder resultBuilder = new StringBuilder();

        for (int index = 0; index < game.getPlayerHands().size(); index++) {

            String handResult =
                    game.resolveGame(index);

            if (index > 0) {
                resultBuilder.append(" | ");
            }

            resultBuilder
                    .append("HAND ")
                    .append(index + 1)
                    .append(": ")
                    .append(handResult);

            if (!gameManager.isTrainerGame(gameId)) {
                gameResultService.processResult(
                        user,
                        handResult,
                        gameManager.getHandBet(gameId, index)
                );
            }
        }

        long coinsAfter = user.getCoins();

        long coinsChange =
                coinsAfter - coinsBefore;

        String result = resultBuilder.toString();

        gameResultService.saveHistory(
                user,
                game.getPlayerHands().toString(),
                game.getDealerHand().toString(),
                result,
                gameManager.isTrainerGame(gameId) ? 0L : coinsChange,
                gameManager.getGameMode(gameId)
        );

        GameStateResponse response =
                toResponse(
                        gameId,
                        game,
                        gameManager.getTotalExposure(gameId),
                        result,
                        user,
                        gameManager.isTrainerGame(gameId) ? 0L : coinsChange
                );

        gameManager.removeGame(gameId);

        return response;
    }

    private GameStateResponse toResponse(
            String gameId,
            BlackjackEngine game,
            Long bet,
            String result,
            User user,
            Long coinsChange
    ) {

        return new GameStateResponse(
                gameId,
                game.getPlayerHands()
                        .stream()
                        .map(GameMapper::toHandResponse)
                        .toList(),
                GameMapper.toDealerHandResponse(
                        game.getDealerHand(),
                        game.getGameStatus().name().equals("PLAYER_TURN")
                ),
                game.getGameStatus().name(),
                bet,
                result,
                user.getCoins(),
                coinsChange,
                game.getCurrentHandIndex(),
                gameManager.getHandBets(gameId),
                ShoeMapper.toResponse(game.getShoe())
        );
    }

    private User getCurrentUser() {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}
