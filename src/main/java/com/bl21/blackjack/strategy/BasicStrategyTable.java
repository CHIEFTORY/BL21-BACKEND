package com.bl21.blackjack.strategy;

import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.enums.MoveAction;
import com.bl21.enums.Rank;

import java.util.HashMap;
import java.util.Map;

public class BasicStrategyTable {

    private static final Map<String, MoveAction> STRATEGY_TABLE = new HashMap<>();

    static {
        buildHardHands();
        buildSoftHands();
        buildPairs();
    }

    private BasicStrategyTable() {
    }

    public static MoveAction getMove(Hand playerHand, Card dealerCard) {
        String key = buildKey(playerHand, dealerCard);

        return STRATEGY_TABLE.getOrDefault(key, MoveAction.HIT);
    }

    public static MoveAction getMove(String key) {
        return STRATEGY_TABLE.getOrDefault(key, MoveAction.HIT);
    }

    private static String buildKey(Hand playerHand, Card dealerCard) {
        int dealerValue = dealerCard.getValue();

        if (playerHand.canSplit()) {
            Rank pairRank = playerHand.getCards().get(0).getRank();

            return "PAIR_" + rankStrategyValue(pairRank) + "_" + dealerValue;
        }

        String handType = playerHand.isSoft() ? "SOFT" : "HARD";

        return handType + "_" + playerHand.calculateValue() + "_" + dealerValue;
    }

    private static int rankStrategyValue(Rank rank) {
        return switch (rank) {
            case ACE -> 11;
            case TWO -> 2;
            case THREE -> 3;
            case FOUR -> 4;
            case FIVE -> 5;
            case SIX -> 6;
            case SEVEN -> 7;
            case EIGHT -> 8;
            case NINE -> 9;
            case TEN, JACK, QUEEN, KING -> 10;
        };
    }

    private static void buildHardHands() {
        for (int total = 4; total <= 8; total++) {
            fill("HARD_" + total, MoveAction.HIT);
        }

        putRange("HARD_9", 3, 6, MoveAction.DOUBLE);
        putAllExcept("HARD_9", MoveAction.HIT, 3, 4, 5, 6);

        putRange("HARD_10", 2, 9, MoveAction.DOUBLE);
        putAllExcept("HARD_10", MoveAction.HIT, 2, 3, 4, 5, 6, 7, 8, 9);

        putRange("HARD_11", 2, 10, MoveAction.DOUBLE);
        put("HARD_11", 11, MoveAction.HIT);

        putRange("HARD_12", 4, 6, MoveAction.STAND);
        putAllExcept("HARD_12", MoveAction.HIT, 4, 5, 6);

        for (int total = 13; total <= 16; total++) {
            putRange("HARD_" + total, 2, 6, MoveAction.STAND);
            putAllExcept("HARD_" + total, MoveAction.HIT, 2, 3, 4, 5, 6);
        }

        for (int total = 17; total <= 21; total++) {
            fill("HARD_" + total, MoveAction.STAND);
        }
    }

    private static void buildSoftHands() {
        putRange("SOFT_13", 5, 6, MoveAction.DOUBLE);
        putAllExcept("SOFT_13", MoveAction.HIT, 5, 6);

        putRange("SOFT_14", 5, 6, MoveAction.DOUBLE);
        putAllExcept("SOFT_14", MoveAction.HIT, 5, 6);

        putRange("SOFT_15", 4, 6, MoveAction.DOUBLE);
        putAllExcept("SOFT_15", MoveAction.HIT, 4, 5, 6);

        putRange("SOFT_16", 4, 6, MoveAction.DOUBLE);
        putAllExcept("SOFT_16", MoveAction.HIT, 4, 5, 6);

        putRange("SOFT_17", 3, 6, MoveAction.DOUBLE);
        putAllExcept("SOFT_17", MoveAction.HIT, 3, 4, 5, 6);

        put("SOFT_18", 2, MoveAction.STAND);
        putRange("SOFT_18", 3, 6, MoveAction.DOUBLE);
        put("SOFT_18", 7, MoveAction.STAND);
        put("SOFT_18", 8, MoveAction.STAND);
        put("SOFT_18", 9, MoveAction.HIT);
        put("SOFT_18", 10, MoveAction.HIT);
        put("SOFT_18", 11, MoveAction.HIT);

        for (int total = 19; total <= 21; total++) {
            fill("SOFT_" + total, MoveAction.STAND);
        }
    }

    private static void buildPairs() {
        fill("PAIR_11", MoveAction.SPLIT);

        putRange("PAIR_10", 2, 11, MoveAction.STAND);

        putRange("PAIR_9", 2, 6, MoveAction.SPLIT);
        put("PAIR_9", 7, MoveAction.STAND);
        put("PAIR_9", 8, MoveAction.SPLIT);
        put("PAIR_9", 9, MoveAction.SPLIT);
        put("PAIR_9", 10, MoveAction.STAND);
        put("PAIR_9", 11, MoveAction.STAND);

        fill("PAIR_8", MoveAction.SPLIT);

        putRange("PAIR_7", 2, 7, MoveAction.SPLIT);
        putAllExcept("PAIR_7", MoveAction.HIT, 2, 3, 4, 5, 6, 7);

        putRange("PAIR_6", 2, 6, MoveAction.SPLIT);
        putAllExcept("PAIR_6", MoveAction.HIT, 2, 3, 4, 5, 6);

        putRange("PAIR_5", 2, 9, MoveAction.DOUBLE);
        put("PAIR_5", 10, MoveAction.HIT);
        put("PAIR_5", 11, MoveAction.HIT);

        put("PAIR_4", 5, MoveAction.SPLIT);
        put("PAIR_4", 6, MoveAction.SPLIT);
        putAllExcept("PAIR_4", MoveAction.HIT, 5, 6);

        putRange("PAIR_3", 2, 7, MoveAction.SPLIT);
        putAllExcept("PAIR_3", MoveAction.HIT, 2, 3, 4, 5, 6, 7);

        putRange("PAIR_2", 2, 7, MoveAction.SPLIT);
        putAllExcept("PAIR_2", MoveAction.HIT, 2, 3, 4, 5, 6, 7);
    }

    private static void fill(String handKey, MoveAction action) {
        for (int dealer = 2; dealer <= 11; dealer++) {
            put(handKey, dealer, action);
        }
    }

    private static void putRange(String handKey, int startDealer, int endDealer, MoveAction action) {
        for (int dealer = startDealer; dealer <= endDealer; dealer++) {
            put(handKey, dealer, action);
        }
    }

    private static void putAllExcept(String handKey, MoveAction action, int... excludedDealers) {
        for (int dealer = 2; dealer <= 11; dealer++) {
            if (!contains(excludedDealers, dealer)) {
                put(handKey, dealer, action);
            }
        }
    }

    private static boolean contains(int[] values, int target) {
        for (int value : values) {
            if (value == target) {
                return true;
            }
        }

        return false;
    }

    private static void put(String handKey, int dealerValue, MoveAction action) {
        STRATEGY_TABLE.put(handKey + "_" + dealerValue, action);
    }
}
