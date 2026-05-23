package com.bl21.controller;

import com.bl21.dto.response.UserProfileResponse;
import com.bl21.entity.User;
import com.bl21.repository.UserRepository;
import com.bl21.service.DailyRewardService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bl21.dto.response.GameHistoryResponse;
import com.bl21.entity.GameHistory;
import com.bl21.repository.GameHistoryRepository;
import java.util.List;
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final DailyRewardService dailyRewardService;
    private final GameHistoryRepository historyRepository;

    public UserController(
            UserRepository userRepository,
            DailyRewardService dailyRewardService,
            GameHistoryRepository historyRepository
    ) {

        this.userRepository = userRepository;
        this.dailyRewardService = dailyRewardService;
        this.historyRepository = historyRepository;
    }

    @GetMapping("/me")
    public UserProfileResponse getProfile() {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return new UserProfileResponse(
                user.getUsername(),
                user.getCoins(),
                user.getTotalGames(),
                user.getTotalWins(),
                user.getTotalLosses(),
                user.getTrainerAccuracy()
        );
    }

    @PostMapping("/daily-reward")
    public String claimDailyReward() {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("User not found"));

        boolean claimed =
                dailyRewardService.claimDailyReward(user);

        if (!claimed) {

            return "Ya reclamaste tu recompensa diaria. Vuelve en 24 horas.";
        }

        return "100000 monedas agregadas correctamente";
    }
    @GetMapping("/history")
    public List<GameHistoryResponse> getHistory() {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("User not found"));

        List<GameHistory> history =
                historyRepository.findTop20ByUserOrderByPlayedAtDesc(user);

        return history.stream()

                .map(game -> new GameHistoryResponse(

                        game.getPlayerHand(),

                        game.getDealerHand(),

                        game.getResult(),

                        game.getCoinsChange(),

                        game.getMode(),

                        game.getPlayedAt()
                ))

                .toList();
    }
}
