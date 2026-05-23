package com.bl21.dto.response;

import java.util.List;

public class StrategyAdviceResponse {

    private String action;

    private String handLabel;

    private String dealerLabel;

    private String explanation;

    private boolean terminal;

    private int dealerBustChance;

    private int hitBustRisk;

    private int confidence;

    private List<StrategyActionScoreResponse> actionScores;

    public StrategyAdviceResponse(
            String action,
            String handLabel,
            String dealerLabel,
            String explanation,
            boolean terminal,
            int dealerBustChance,
            int hitBustRisk,
            int confidence,
            List<StrategyActionScoreResponse> actionScores
    ) {
        this.action = action;
        this.handLabel = handLabel;
        this.dealerLabel = dealerLabel;
        this.explanation = explanation;
        this.terminal = terminal;
        this.dealerBustChance = dealerBustChance;
        this.hitBustRisk = hitBustRisk;
        this.confidence = confidence;
        this.actionScores = actionScores;
    }

    public String getAction() {
        return action;
    }

    public String getHandLabel() {
        return handLabel;
    }

    public String getDealerLabel() {
        return dealerLabel;
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public int getDealerBustChance() {
        return dealerBustChance;
    }

    public int getHitBustRisk() {
        return hitBustRisk;
    }

    public int getConfidence() {
        return confidence;
    }

    public List<StrategyActionScoreResponse> getActionScores() {
        return actionScores;
    }
}
