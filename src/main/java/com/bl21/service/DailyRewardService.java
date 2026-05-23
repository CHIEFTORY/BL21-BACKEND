package com.bl21.service;

import com.bl21.entity.User;
import com.bl21.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DailyRewardService {

    private static final long DAILY_REWARD = 100000L;

    private final UserRepository userRepository;

    public DailyRewardService(
            UserRepository userRepository
    ) {

        this.userRepository = userRepository;
    }

    public boolean claimDailyReward(User user) {

        LocalDateTime now = LocalDateTime.now();

        if (user.getLastDailyReward() != null) {

            LocalDateTime nextReward =
                    user.getLastDailyReward().plusHours(24);

            if (now.isBefore(nextReward)) {

                return false;
            }
        }

        user.setCoins(
                user.getCoins() + DAILY_REWARD
        );

        user.setLastDailyReward(now);

        userRepository.save(user);

        return true;
    }
}