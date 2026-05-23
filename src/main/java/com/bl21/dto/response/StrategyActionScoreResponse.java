package com.bl21.dto.response;

public class StrategyActionScoreResponse {

    private String action;

    private double expectedValue;

    private int winChance;

    private int pushChance;

    private int loseChance;

    private boolean legal;

    public StrategyActionScoreResponse(
            String action,
            double expectedValue,
            int winChance,
            int pushChance,
            int loseChance,
            boolean legal
    ) {
        this.action = action;
        this.expectedValue = expectedValue;
        this.winChance = winChance;
        this.pushChance = pushChance;
        this.loseChance = loseChance;
        this.legal = legal;
    }

    public String getAction() {
        return action;
    }

    public double getExpectedValue() {
        return expectedValue;
    }

    public int getWinChance() {
        return winChance;
    }

    public int getPushChance() {
        return pushChance;
    }

    public int getLoseChance() {
        return loseChance;
    }

    public boolean isLegal() {
        return legal;
    }
}
