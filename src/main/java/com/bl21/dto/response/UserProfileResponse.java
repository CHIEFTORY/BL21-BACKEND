package com.bl21.dto.response;

public class UserProfileResponse {

    private String username;

    private Long coins;

    private Integer totalGames;

    private Integer totalWins;

    private Integer totalLosses;

    private Double trainerAccuracy;

    public UserProfileResponse(
            String username,
            Long coins,
            Integer totalGames,
            Integer totalWins,
            Integer totalLosses,
            Double trainerAccuracy
    ) {

        this.username = username;
        this.coins = coins;
        this.totalGames = totalGames;
        this.totalWins = totalWins;
        this.totalLosses = totalLosses;
        this.trainerAccuracy = trainerAccuracy;
    }

    public String getUsername() {
        return username;
    }

    public Long getCoins() {
        return coins;
    }

    public Integer getTotalGames() {
        return totalGames;
    }

    public Integer getTotalWins() {
        return totalWins;
    }

    public Integer getTotalLosses() {
        return totalLosses;
    }

    public Double getTrainerAccuracy() {
        return trainerAccuracy;
    }
}