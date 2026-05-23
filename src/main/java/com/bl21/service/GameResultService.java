package com.bl21.service;
import com.bl21.entity.GameHistory;
import com.bl21.repository.GameHistoryRepository;
import com.bl21.entity.User;
import com.bl21.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class GameResultService {

    private final UserRepository userRepository;
    private final GameHistoryRepository historyRepository;

    public GameResultService(
            UserRepository userRepository,
            GameHistoryRepository historyRepository
    ) {

        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
    }

    public void processResult(
            User user,
            String result
    ) {

        processResult(
                user,
                result,
                100L
        );
    }

    public void processResult(
            User user,
            String result,
            Long bet
    ) {

        user.setTotalGames(
                user.getTotalGames() + 1
        );

        switch (result) {

            case "PLAYER WINS":
            case "DEALER BUSTS - PLAYER WINS":

                user.setTotalWins(
                        user.getTotalWins() + 1
                );

                user.setCoins(
                        user.getCoins() + bet
                );

                break;

            case "BLACKJACK":

                user.setTotalWins(
                        user.getTotalWins() + 1
                );

                user.setCoins(
                        user.getCoins() + Math.round(bet * 1.5)
                );

                break;

            case "DEALER WINS":
            case "PLAYER BUSTS - DEALER WINS":

                user.setTotalLosses(
                        user.getTotalLosses() + 1
                );

                user.setCoins(
                        user.getCoins() - bet
                );

                break;

            case "PUSH":

                user.setTotalPushes(
                        user.getTotalPushes() + 1
                );

                break;
        }

        userRepository.save(user);
    }

    public void saveHistory(
            User user,
            String playerHand,
            String dealerHand,
            String result,
            Long coinsChange
    ) {

        saveHistory(
                user,
                playerHand,
                dealerHand,
                result,
                coinsChange,
                "SOLO"
        );
    }

    public void saveHistory(
            User user,
            String playerHand,
            String dealerHand,
            String result,
            Long coinsChange,
            String mode
    ) {

        GameHistory history =
                new GameHistory();

        history.setUser(user);

        history.setPlayerHand(playerHand);

        history.setDealerHand(dealerHand);

        history.setResult(result);

        history.setCoinsChange(coinsChange);

        history.setMode(mode);

        historyRepository.save(history);
    }
}
