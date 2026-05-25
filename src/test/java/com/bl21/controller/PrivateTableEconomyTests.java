package com.bl21.controller;

import com.bl21.dto.request.CreateTableRequest;
import com.bl21.dto.response.PrivateTableResponse;
import com.bl21.entity.User;
import com.bl21.repository.UserRepository;
import com.bl21.service.PrivateTableHistoryService;
import com.bl21.websocket.PrivateTableManager;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrivateTableEconomyTests {

    @Test
    void createTableDeductsBuyInAndLeaveReturnsRemainingStack() {
        PrivateTableManager tableManager = new PrivateTableManager();
        UserRepository userRepository = mock(UserRepository.class);
        PrivateTableHistoryService historyService = mock(PrivateTableHistoryService.class);
        PrivateTableController controller =
                new PrivateTableController(tableManager, userRepository, historyService);
        User user = new User();
        user.setUsername("maurix");
        user.setCoins(5000L);
        CreateTableRequest request = new CreateTableRequest();
        request.setBuyIn(1000L);

        when(userRepository.findByUsername("maurix"))
                .thenReturn(Optional.of(user));
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("maurix", "password"));

        PrivateTableResponse response =
                controller.createTable(request);

        assertEquals(4000L, user.getCoins());

        controller.leaveTable(response.getTableId());

        assertEquals(5000L, user.getCoins());
        verify(userRepository, times(2)).save(user);
    }

    @Test
    void joinTableUsesGuestBuyInAndReloadStackDeductsCoins() {
        PrivateTableManager tableManager = new PrivateTableManager();
        UserRepository userRepository = mock(UserRepository.class);
        PrivateTableHistoryService historyService = mock(PrivateTableHistoryService.class);
        PrivateTableController controller =
                new PrivateTableController(tableManager, userRepository, historyService);
        User host = new User();
        host.setUsername("host");
        host.setCoins(10000L);
        User guest = new User();
        guest.setUsername("guest");
        guest.setCoins(10000L);
        CreateTableRequest request = new CreateTableRequest();
        request.setBuyIn(1000L);

        when(userRepository.findByUsername("host"))
                .thenReturn(Optional.of(host));
        when(userRepository.findByUsername("guest"))
                .thenReturn(Optional.of(guest));

        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("host", "password"));
        PrivateTableResponse response =
                controller.createTable(request);

        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("guest", "password"));
        controller.joinTable(response.getTableId(), Map.of("buyIn", 2500L));

        assertEquals(7500L, guest.getCoins());
        assertEquals(2500L, tableManager.getTable(response.getTableId())
                .getPlayers()
                .stream()
                .filter(player -> player.getUsername().equals("guest"))
                .findFirst()
                .orElseThrow()
                .getStack());

        controller.reloadStack(response.getTableId(), Map.of("amount", 500L));

        assertEquals(7000L, guest.getCoins());
        assertEquals(3000L, tableManager.getTable(response.getTableId())
                .getPlayers()
                .stream()
                .filter(player -> player.getUsername().equals("guest"))
                .findFirst()
                .orElseThrow()
                .getStack());
    }
}
