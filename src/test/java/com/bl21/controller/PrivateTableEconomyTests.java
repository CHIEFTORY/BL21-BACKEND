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
}
