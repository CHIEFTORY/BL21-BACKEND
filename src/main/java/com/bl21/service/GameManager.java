package com.bl21.service;

import com.bl21.blackjack.engine.BlackjackEngine;
import com.bl21.blackjack.deck.Shoe;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GameManager {

    private final Map<String, BlackjackEngine> activeGames =
            new HashMap<>();

    private final Map<String, Long> activeBets =
            new HashMap<>();

    private final Map<String, List<Long>> activeHandBets =
            new HashMap<>();

    private final Map<String, String> activeGameModes =
            new HashMap<>();

    private final Map<String, String> gameOwners =
            new HashMap<>();

    private final Map<String, String> activeSoloGameByUser =
            new HashMap<>();

    private final Map<String, Shoe> soloShoesByUser =
            new HashMap<>();

    private final Map<String, Shoe> trainerShoesByUser =
            new HashMap<>();

    public String createGame() {

        return createGame(100L);
    }

    public String createGame(
            Long bet
    ) {

        return createGame(
                "default",
                bet
        );
    }

    public String createGame(
            String username,
            Long bet
    ) {

        String activeGameId =
                activeSoloGameByUser.get(username);

        if (activeGameId != null
                && activeGames.containsKey(activeGameId)) {
            return activeGameId;
        }

        Shoe shoe =
                soloShoesByUser.computeIfAbsent(
                        username,
                        key -> new Shoe(6)
                );

        BlackjackEngine game = new BlackjackEngine(shoe);

        game.startGame();

        String gameId = UUID.randomUUID().toString();

        activeGames.put(gameId, game);

        activeBets.put(gameId, bet);

        activeHandBets.put(gameId, new ArrayList<>(List.of(bet)));

        activeGameModes.put(gameId, "SOLO");

        gameOwners.put(gameId, username);

        activeSoloGameByUser.put(username, gameId);

        return gameId;
    }

    public String createTrainerGame(
            String mode
    ) {

        return createTrainerGame(
                "default",
                mode
        );
    }

    public String createTrainerGame(
            String username,
            String mode
    ) {

        String normalizedMode = mode == null
                ? "all"
                : mode.toLowerCase();

        Shoe trainerShoe =
                trainerShoesByUser.computeIfAbsent(
                        username,
                        key -> new Shoe(6)
                );

        for (int attempt = 0; attempt < 300; attempt++) {

            BlackjackEngine candidate = new BlackjackEngine();

            candidate.startGame();

            if (matchesTrainerMode(candidate, normalizedMode)) {

                BlackjackEngine game = new BlackjackEngine(trainerShoe);

                game.startPresetGame(
                        candidate.getCurrentHand(),
                        candidate.getDealerHand()
                );

                String gameId = UUID.randomUUID().toString();

                activeGames.put(gameId, game);

                activeBets.put(gameId, 100L);

                activeHandBets.put(gameId, new ArrayList<>(List.of(100L)));

                activeGameModes.put(gameId, "TRAINER");

                gameOwners.put(gameId, username);

                return gameId;
            }
        }

        throw new RuntimeException("Could not create trainer hand for mode " + normalizedMode);
    }

    private boolean matchesTrainerMode(
            BlackjackEngine game,
            String mode
    ) {

        var hand = game.getCurrentHand();

        if (hand.isBlackjack() || game.getDealerHand().isBlackjack()) {
            return false;
        }

        return switch (mode) {
            case "hard" -> !hand.canSplit() && !hand.isSoft();
            case "soft" -> !hand.canSplit() && hand.isSoft();
            case "pairs" -> hand.canSplit();
            default -> true;
        };
    }

    public BlackjackEngine getGame(String gameId) {

        BlackjackEngine game = activeGames.get(gameId);

        if (game == null) {
            throw new RuntimeException("Game not found");
        }

        return game;
    }

    public String getActiveSoloGameId(String username) {

        String gameId =
                activeSoloGameByUser.get(username);

        if (gameId != null
                && activeGames.containsKey(gameId)) {
            return gameId;
        }

        return null;
    }

    public void removeGame(String gameId) {

        String username =
                gameOwners.remove(gameId);

        if ("SOLO".equals(activeGameModes.get(gameId))
                && username != null) {
            activeSoloGameByUser.remove(username, gameId);
        }

        activeGames.remove(gameId);

        activeBets.remove(gameId);

        activeHandBets.remove(gameId);

        activeGameModes.remove(gameId);
    }

    public boolean isTrainerGame(String gameId) {
        return "TRAINER".equals(activeGameModes.get(gameId));
    }

    public String getGameMode(String gameId) {
        return activeGameModes.getOrDefault(gameId, "SOLO");
    }

    public Long getBet(String gameId) {

        return activeBets.getOrDefault(gameId, 100L);
    }

    public Long getHandBet(String gameId, int handIndex) {

        List<Long> handBets = activeHandBets.get(gameId);

        if (handBets == null || handIndex >= handBets.size()) {
            return getBet(gameId);
        }

        return handBets.get(handIndex);
    }

    public Long getTotalExposure(String gameId) {

        List<Long> handBets = activeHandBets.get(gameId);

        if (handBets == null || handBets.isEmpty()) {
            return getBet(gameId);
        }

        return handBets
                .stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    public List<Long> getHandBets(String gameId) {

        List<Long> handBets = activeHandBets.get(gameId);

        if (handBets == null || handBets.isEmpty()) {
            return List.of(getBet(gameId));
        }

        return List.copyOf(handBets);
    }

    public void splitHandBet(String gameId, int handIndex) {

        List<Long> handBets = activeHandBets.get(gameId);

        if (handBets == null) {
            handBets = new ArrayList<>(List.of(getBet(gameId)));
            activeHandBets.put(gameId, handBets);
        }

        Long handBet = handBets.get(handIndex);

        handBets.add(handIndex + 1, handBet);
    }

    public void doubleHandBet(String gameId, int handIndex) {

        List<Long> handBets = activeHandBets.get(gameId);

        if (handBets == null) {
            handBets = new ArrayList<>(List.of(getBet(gameId)));
            activeHandBets.put(gameId, handBets);
        }

        handBets.set(handIndex, handBets.get(handIndex) * 2);
    }
}
