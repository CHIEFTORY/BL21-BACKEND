package com.bl21.repository;

import com.bl21.entity.GameHistory;
import com.bl21.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameHistoryRepository
        extends JpaRepository<GameHistory, Long> {

    List<GameHistory> findByUser(User user);

    List<GameHistory> findTop20ByUserOrderByPlayedAtDesc(User user);
}
