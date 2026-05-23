package com.bl21.repository;

import com.bl21.entity.PlayerMove;
import com.bl21.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerMoveRepository
        extends JpaRepository<PlayerMove, Long> {

    List<PlayerMove> findByUser(User user);

    List<PlayerMove> findByUserAndCorrectFalse(User user);
}
