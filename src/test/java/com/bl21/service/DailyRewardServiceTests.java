package com.bl21.service;

import com.bl21.entity.User;
import com.bl21.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DailyRewardServiceTests {

    @Test
    void firstDailyRewardAddsCoinsAndStoresClaimTime() {
        UserRepository userRepository = mock(UserRepository.class);
        DailyRewardService service = new DailyRewardService(userRepository);
        User user = new User();
        user.setCoins(500L);

        boolean claimed = service.claimDailyReward(user);

        assertTrue(claimed);
        assertEquals(100500L, user.getCoins());
        verify(userRepository).save(user);
    }

    @Test
    void dailyRewardCannotBeClaimedBeforeTwentyFourHours() {
        UserRepository userRepository = mock(UserRepository.class);
        DailyRewardService service = new DailyRewardService(userRepository);
        User user = new User();
        user.setCoins(500L);
        user.setLastDailyReward(LocalDateTime.now().minusHours(2));

        boolean claimed = service.claimDailyReward(user);

        assertFalse(claimed);
        assertEquals(500L, user.getCoins());
        verify(userRepository, never()).save(user);
    }

    @Test
    void dailyRewardCanBeClaimedAfterTwentyFourHours() {
        UserRepository userRepository = mock(UserRepository.class);
        DailyRewardService service = new DailyRewardService(userRepository);
        User user = new User();
        user.setCoins(500L);
        user.setLastDailyReward(LocalDateTime.now().minusHours(25));

        boolean claimed = service.claimDailyReward(user);

        assertTrue(claimed);
        assertEquals(100500L, user.getCoins());
        verify(userRepository).save(user);
    }
}
