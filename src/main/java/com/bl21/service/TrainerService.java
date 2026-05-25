package com.bl21.service;

import com.bl21.blackjack.strategy.StrategyValidator;
import com.bl21.dto.response.StrategyActionScoreResponse;
import com.bl21.dto.response.StrategyAdviceResponse;
import com.bl21.dto.response.TrainerMistakeResponse;
import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.entity.PlayerMove;
import com.bl21.entity.User;
import com.bl21.enums.MoveAction;
import com.bl21.repository.PlayerMoveRepository;
import com.bl21.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrainerService {

    private final StrategyValidator validator;

    private final PlayerMoveRepository moveRepository;

    private final UserRepository userRepository;

    public TrainerService(
            PlayerMoveRepository moveRepository,
            UserRepository userRepository
    ) {

        this.validator = new StrategyValidator();

        this.moveRepository = moveRepository;

        this.userRepository = userRepository;
    }

    public boolean validateMove(
            Hand playerHand,
            Card dealerCard,
            MoveAction move
    ) {

        MoveAction correctMove =
                validator.getCorrectMove(
                        playerHand,
                        dealerCard
                );

        boolean correct = correctMove == move;

        saveMove(
                playerHand,
                dealerCard,
                move,
                correctMove,
                correct
        );

        return correct;
    }

    public MoveAction getCorrectMove(
            Hand playerHand,
            Card dealerCard
    ) {

        return validator.getCorrectMove(
                playerHand,
                dealerCard
        );
    }

    public List<TrainerMistakeResponse> getFrequentMistakes() {

        User user = getCurrentUser();

        return moveRepository
                .findByUserAndCorrectFalse(user)
                .stream()
                .collect(Collectors.groupingBy(
                        move -> String.join(
                                "|",
                                move.getPlayerHand(),
                                move.getDealerCard(),
                                move.getPlayerMove(),
                                move.getCorrectMove()
                        ),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(6)
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|", -1);

                    return new TrainerMistakeResponse(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            entry.getValue()
                    );
                })
                .toList();
    }

    public StrategyAdviceResponse getStrategyAdvice(
            Hand playerHand,
            Card dealerCard
    ) {

        int playerValue = playerHand.calculateValue();
        int dealerValue = dealerCard.getValue();
        String dealerLabel = formatDealerLabel(dealerValue);

        if (playerHand.isBust()) {
            return new StrategyAdviceResponse(
                    "BUST",
                    "BUST " + playerValue,
                    dealerLabel,
                    "La mano ya paso de 21. No hay jugada pendiente; esta decision termino.",
                    true,
                    0,
                    100,
                    0,
                    buildTerminalScores()
            );
        }

        if (playerHand.isTwentyOne()) {
            return new StrategyAdviceResponse(
                    "STAND",
                    "21",
                    dealerLabel,
                    "Llegaste a 21. La mano queda cerrada automaticamente; no debes pedir mas cartas.",
                    true,
                    estimateDealerBustChance(dealerValue),
                    100,
                    100,
                    buildTerminalScores()
            );
        }

        MoveAction correctMove = getCorrectMove(playerHand, dealerCard);
        String handLabel = buildHandLabel(playerHand);
        int dealerBustChance = estimateDealerBustChance(dealerValue);
        int hitBustRisk = estimateHitBustRisk(playerHand);
        int confidence = estimateConfidence(correctMove, dealerBustChance, hitBustRisk);

        return new StrategyAdviceResponse(
                correctMove.name(),
                handLabel,
                dealerLabel,
                buildExplanation(correctMove, playerHand, dealerValue, dealerBustChance, hitBustRisk),
                false,
                dealerBustChance,
                hitBustRisk,
                confidence,
                buildActionScores(playerHand, dealerCard)
        );
    }

    private List<StrategyActionScoreResponse> buildTerminalScores() {
        return List.of(
                new StrategyActionScoreResponse("HIT", -1.0, 0, 0, 100, false),
                new StrategyActionScoreResponse("STAND", -1.0, 0, 0, 100, false),
                new StrategyActionScoreResponse("DOUBLE", -1.0, 0, 0, 100, false),
                new StrategyActionScoreResponse("SPLIT", -1.0, 0, 0, 100, false)
        );
    }

    private List<StrategyActionScoreResponse> buildActionScores(
            Hand playerHand,
            Card dealerCard
    ) {

        List<StrategyActionScoreResponse> scores = new ArrayList<>();

        scores.add(scoreAction("HIT", estimateHitEv(playerHand, dealerCard), true));
        scores.add(scoreAction("STAND", estimateStandEv(playerHand, dealerCard), true));
        scores.add(scoreAction("DOUBLE", estimateHitEv(playerHand, dealerCard) * 2, playerHand.canDouble()));
        scores.add(scoreAction("SPLIT", estimateSplitEv(playerHand, dealerCard), playerHand.canSplit()));

        return scores;
    }

    private StrategyActionScoreResponse scoreAction(
            String action,
            double expectedValue,
            boolean legal
    ) {

        double normalized = legal ? clamp(expectedValue, -1.0, 1.0) : -1.0;
        int winChance = legal ? (int) Math.round(Math.max(0, normalized) * 52 + 30) : 0;
        int loseChance = legal ? (int) Math.round(Math.max(0, -normalized) * 52 + 30) : 100;
        int pushChance = legal ? Math.max(0, 100 - winChance - loseChance) : 0;

        if (legal && normalized > 0.6) {
            winChance = Math.min(92, winChance);
            loseChance = Math.max(4, 100 - winChance - pushChance);
        }

        return new StrategyActionScoreResponse(
                action,
                Math.round(normalized * 1000.0) / 1000.0,
                winChance,
                pushChance,
                loseChance,
                legal
        );
    }

    private double estimateStandEv(
            Hand playerHand,
            Card dealerCard
    ) {

        int playerValue = playerHand.calculateValue();

        if (playerValue > 21) {
            return -1.0;
        }

        return estimateDealerOutcomeEv(
                playerValue,
                dealerCard.getValue(),
                dealerCard.isAce()
        );
    }

    private double estimateHitEv(
            Hand playerHand,
            Card dealerCard
    ) {

        double ev = 0.0;

        for (int cardValue = 1; cardValue <= 10; cardValue++) {
            Hand nextHand = copyHand(playerHand);
            nextHand.addCard(simulatedCard(cardValue));

            double probability = cardProbability(cardValue);

            if (nextHand.isBust()) {
                ev -= probability;
            } else {
                ev += probability * estimateStandEv(nextHand, dealerCard);
            }
        }

        return ev;
    }

    private double estimateSplitEv(
            Hand playerHand,
            Card dealerCard
    ) {

        if (!playerHand.canSplit()) {
            return -1.0;
        }

        Card splitCard = playerHand.getCards().get(0);
        double oneHandEv = 0.0;

        for (int cardValue = 1; cardValue <= 10; cardValue++) {
            Hand splitHand = new Hand();
            splitHand.addCard(splitCard);
            splitHand.addCard(simulatedCard(cardValue));

            oneHandEv += cardProbability(cardValue)
                    * Math.max(
                    estimateStandEv(splitHand, dealerCard),
                    estimateHitEv(splitHand, dealerCard)
            );
        }

        return oneHandEv;
    }

    private double estimateDealerOutcomeEv(
            int playerValue,
            int dealerTotal,
            boolean soft
    ) {

        if (dealerTotal > 21) {
            return 1.0;
        }

        if (dealerTotal >= 17) {
            if (playerValue > dealerTotal) {
                return 1.0;
            }

            if (playerValue == dealerTotal) {
                return 0.0;
            }

            return -1.0;
        }

        double ev = 0.0;

        for (int cardValue = 1; cardValue <= 10; cardValue++) {
            int nextTotal = dealerTotal + normalizedCardValue(cardValue);
            boolean nextSoft = soft || cardValue == 1;

            while (nextTotal > 21 && nextSoft) {
                nextTotal -= 10;
                nextSoft = false;
            }

            ev += cardProbability(cardValue)
                    * estimateDealerOutcomeEv(playerValue, nextTotal, nextSoft);
        }

        return ev;
    }

    private Hand copyHand(Hand source) {
        Hand hand = new Hand();

        source.getCards().forEach(hand::addCard);

        return hand;
    }

    private Card simulatedCard(int cardValue) {
        com.bl21.enums.Rank rank = switch (cardValue) {
            case 1 -> com.bl21.enums.Rank.ACE;
            case 2 -> com.bl21.enums.Rank.TWO;
            case 3 -> com.bl21.enums.Rank.THREE;
            case 4 -> com.bl21.enums.Rank.FOUR;
            case 5 -> com.bl21.enums.Rank.FIVE;
            case 6 -> com.bl21.enums.Rank.SIX;
            case 7 -> com.bl21.enums.Rank.SEVEN;
            case 8 -> com.bl21.enums.Rank.EIGHT;
            case 9 -> com.bl21.enums.Rank.NINE;
            default -> com.bl21.enums.Rank.TEN;
        };

        return new Card(com.bl21.enums.Suit.SPADES, rank);
    }

    private int normalizedCardValue(int cardValue) {
        return cardValue == 1 ? 11 : cardValue;
    }

    private double cardProbability(int cardValue) {
        return cardValue == 10 ? 4.0 / 13.0 : 1.0 / 13.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String buildHandLabel(Hand playerHand) {

        if (playerHand.canSplit()) {
            int pairValue = playerHand.getCards().get(0).getValue();

            return "Par " + formatDealerLabel(pairValue);
        }

        String handType = playerHand.isSoft() ? "Suave" : "Dura";

        return handType + " " + playerHand.calculateValue();
    }

    private String buildExplanation(
            MoveAction correctMove,
            Hand playerHand,
            int dealerValue,
            int dealerBustChance,
            int hitBustRisk
    ) {

        String dealerLabel = formatDealerLabel(dealerValue);

        if (correctMove == MoveAction.SPLIT) {
            return "Divide porque tienes un par inicial y contra " + dealerLabel + " gana mas valor separarlo.";
        }

        if (correctMove == MoveAction.DOUBLE) {
            return "Dobla porque tu mano tiene ventaja contra " + dealerLabel + " y conviene aumentar la apuesta.";
        }

        if (correctMove == MoveAction.STAND) {
            return "Plantate: el dealer muestra " + dealerLabel + ", su riesgo de pasarse ronda " + dealerBustChance + "% y pedir tiene riesgo " + hitBustRisk + "%.";
        }

        if (playerHand.isSoft()) {
            return "Pide porque tu As flexible protege la mano y necesitas mejorar contra " + dealerLabel + ".";
        }

        return "Pide porque tu total no aguanta bien plantarse contra " + dealerLabel + ". Riesgo estimado al pedir: " + hitBustRisk + "%.";
    }

    private int estimateDealerBustChance(int dealerValue) {

        return switch (dealerValue) {
            case 2 -> 35;
            case 3 -> 37;
            case 4 -> 40;
            case 5, 6 -> 42;
            case 7 -> 26;
            case 8 -> 24;
            case 9 -> 23;
            case 10 -> 21;
            case 11 -> 12;
            default -> 20;
        };
    }

    private int estimateHitBustRisk(Hand playerHand) {

        if (playerHand.isSoft()) {
            return 0;
        }

        int total = playerHand.calculateValue();

        if (total <= 11) {
            return 0;
        }

        if (total >= 21) {
            return 100;
        }

        int bustValues = Math.max(0, total - 11);

        return Math.round((bustValues / 10.0f) * 77);
    }

    private int estimateConfidence(
            MoveAction correctMove,
            int dealerBustChance,
            int hitBustRisk
    ) {

        if (correctMove == MoveAction.STAND) {
            return Math.min(96, 54 + dealerBustChance);
        }

        if (correctMove == MoveAction.DOUBLE) {
            return Math.min(92, 60 + Math.round(dealerBustChance / 2.0f));
        }

        if (correctMove == MoveAction.SPLIT) {
            return 86;
        }

        return Math.max(54, 92 - hitBustRisk);
    }

    private String formatDealerLabel(int value) {

        if (value == 11) {
            return "A";
        }

        return String.valueOf(value);
    }

    private void saveMove(
            Hand playerHand,
            Card dealerCard,
            MoveAction playerMove,
            MoveAction correctMove,
            boolean correct
    ) {

        User user = getCurrentUser();

        PlayerMove move = new PlayerMove();

        move.setUser(user);

        move.setPlayerHand(
                playerHand.toString()
        );

        move.setDealerCard(
                dealerCard.toString()
        );

        move.setPlayerMove(
                playerMove.name()
        );

        move.setAction(
                playerMove.name()
        );

        move.setCorrectMove(
                correctMove.name()
        );

        move.setCorrect(correct);

        move.setMoveNumber(
                moveRepository.findByUser(user).size() + 1
        );

        moveRepository.save(move);
        updateUserAccuracy(user);
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

    private void updateUserAccuracy(User user) {

        long totalMoves =
                moveRepository.findByUser(user).size();

        long correctMoves =
                moveRepository
                        .findByUser(user)
                        .stream()
                        .filter(PlayerMove::getCorrect)
                        .count();

        double accuracy = 0.0;

        if (totalMoves > 0) {

            accuracy =
                    ((double) correctMoves / totalMoves) * 100;
        }

        user.setTrainerAccuracy(accuracy);

        userRepository.save(user);
    }
}
