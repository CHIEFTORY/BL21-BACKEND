package com.bl21.controller;

import com.bl21.dto.response.PrivateTableResponse;
import com.bl21.dto.response.PrivateTableStateResponse;
import com.bl21.dto.request.TableActionRequest;
import com.bl21.mapper.PrivateTableMapper;
import com.bl21.websocket.PrivateTable;
import com.bl21.websocket.PrivateTableManager;
import com.bl21.websocket.TablePlayer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.bl21.dto.response.GameStateResponse;
import com.bl21.mapper.GameMapper;

import com.bl21.dto.request.CreateTableRequest;
import com.bl21.entity.User;
import com.bl21.repository.UserRepository;
import com.bl21.service.PrivateTableHistoryService;
import java.util.Map;

@RestController
@RequestMapping("/tables")
public class PrivateTableController {

    private final PrivateTableManager tableManager;
    private final UserRepository userRepository;
    private final PrivateTableHistoryService privateTableHistoryService;

    public PrivateTableController(
            PrivateTableManager tableManager,
            UserRepository userRepository,
            PrivateTableHistoryService privateTableHistoryService
    ) {

        this.tableManager = tableManager;
        this.userRepository = userRepository;
        this.privateTableHistoryService = privateTableHistoryService;
    }

    @PostMapping("/create")
    public PrivateTableResponse createTable(
            @RequestBody CreateTableRequest request
    ) {

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

        Long buyIn = request.getBuyIn();

        if (buyIn == null || buyIn < 100) {
            throw new RuntimeException("Invalid buy-in");
        }

        if (user.getCoins() < request.getBuyIn()) {

            throw new RuntimeException(
                    "Not enough coins"
            );
        }

        user.setCoins(
                user.getCoins() - buyIn
        );

        userRepository.save(user);

        PrivateTable table =
                tableManager.createTable(
                        username,
                        buyIn
                );

        return new PrivateTableResponse(

                table.getTableId(),

                table.getHostUsername(),

                table.getPlayers()
                        .stream()
                        .map(TablePlayer::getUsername)
                        .toList()
        );
    }

    @PostMapping("/{tableId}/join")
    public PrivateTableResponse joinTable(
            @PathVariable String tableId,
            @RequestBody(required = false) Map<String, Long> body
    ) {

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

        PrivateTable existingTable =
                tableManager.getTable(tableId);

        existingTable.resetFinishedRoundIfNeeded();

        Long buyIn =
                body != null
                        ? body.get("buyIn")
                        : null;

        if (buyIn == null) {
            buyIn = existingTable.getBuyIn();
        }

        if (buyIn < 100) {
            throw new RuntimeException("Invalid buy-in");
        }

        if (existingTable.isRoundStarted()) {
            throw new RuntimeException(
                    "Round already started"
            );
        }

        if (user.getCoins() < buyIn) {

            throw new RuntimeException(
                    "Not enough coins"
            );
        }

        user.setCoins(
                user.getCoins() - buyIn
        );

        userRepository.save(user);

        PrivateTable table =
                null;

        try {
            table =
                    tableManager.joinTable(
                            tableId,
                            username,
                            buyIn
                    );
        } catch (RuntimeException error) {
            user.setCoins(
                    user.getCoins()
                            + buyIn
            );

            userRepository.save(user);

            throw error;
        }

        return new PrivateTableResponse(

                table.getTableId(),

                table.getHostUsername(),

                table.getPlayers()
                        .stream()
                        .map(TablePlayer::getUsername)
                        .toList()
        );
    }

    @PostMapping("/{tableId}/reload-stack")
    public PrivateTableStateResponse reloadStack(
            @PathVariable String tableId,
            @RequestBody Map<String, Long> body
    ) {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Long amount =
                body.get("amount");

        if (amount == null || amount < 100) {
            throw new RuntimeException("Invalid reload amount");
        }

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("User not found"));

        PrivateTable table =
                tableManager.getTable(tableId);

        table.resetFinishedRoundIfNeeded();

        if (table.isRoundStarted()) {
            throw new RuntimeException("Cannot reload during an active round");
        }

        if (user.getCoins() < amount) {
            throw new RuntimeException("Not enough coins");
        }

        TablePlayer player =
                table.getPlayers()
                        .stream()
                        .filter(p ->
                                p.getUsername()
                                        .equals(username)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Player not found"
                                )
                        );

        user.setCoins(
                user.getCoins() - amount
        );

        player.addStack(amount);

        userRepository.save(user);

        table.updateCountdownState();

        return PrivateTableMapper.toStateResponse(table);
    }

    @GetMapping("/{tableId}/state")
    public PrivateTableStateResponse getTableState(
            @PathVariable String tableId
    ) {

        PrivateTable table =
                tableManager.getTable(tableId);

        table.updateCountdownState();

        privateTableHistoryService.saveIfNeeded(table);

        return PrivateTableMapper.toStateResponse(table);
    }

    @PostMapping("/{tableId}/leave")
    public String leaveTable(
            @PathVariable String tableId
    ) {

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

        PrivateTable table =
                tableManager.getTable(tableId);

        TablePlayer leavingPlayer =
                table.getPlayers()
                        .stream()
                        .filter(player ->
                                player.getUsername()
                                        .equals(username)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException("Player not in table"));

        user.setCoins(
                user.getCoins()
                        + leavingPlayer.getStack()
        );

        userRepository.save(user);

        table.removePlayer(username);

        if (!table.getPlayers().isEmpty()) {
            table.updateCountdownState();
        }

        if (table.getPlayers().isEmpty()) {

            tableManager.removeTable(tableId);
        }

        return "Player left table";
    }
    @PostMapping("/{tableId}/bet")
    public String placeBet(
            @PathVariable String tableId,
            @RequestBody Map<String, Long> body
    ) {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Long amount =
                body.get("amount");

        PrivateTable table =
                tableManager.getTable(tableId);

        table.resetFinishedRoundIfNeeded();

        TablePlayer player =
                table.getPlayers()
                        .stream()
                        .filter(p ->
                                p.getUsername()
                                        .equals(username)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Player not found"
                                )
                        );

        player.placeBet(amount);

        table.updateCountdownState();

        return "Bet placed successfully";
    }

    @PostMapping("/{tableId}/enter-round")
    public PrivateTableStateResponse enterRound(
            @PathVariable String tableId,
            @RequestBody Map<String, Long> body
    ) {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Long amount =
                body.get("amount");

        PrivateTable table =
                tableManager.getTable(tableId);

        table.resetFinishedRoundIfNeeded();

        if (table.isRoundStarted()) {
            throw new RuntimeException("Round already started");
        }

        TablePlayer player =
                table.getPlayers()
                        .stream()
                        .filter(p ->
                                p.getUsername()
                                        .equals(username)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Player not found"
                                )
                        );

        if (player.getCurrentBet() > 0) {
            player.cancelRoundEntry();
        }

        player.placeBet(amount);

        player.setReady(true);

        table.updateCountdownState();

        return PrivateTableMapper.toStateResponse(table);
    }

    @PostMapping("/{tableId}/cancel-entry")
    public PrivateTableStateResponse cancelRoundEntry(
            @PathVariable String tableId
    ) {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        PrivateTable table =
                tableManager.getTable(tableId);

        if (table.isRoundStarted()) {
            throw new RuntimeException("Round already started");
        }

        TablePlayer player =
                table.getPlayers()
                        .stream()
                        .filter(p ->
                                p.getUsername()
                                        .equals(username)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Player not found"
                                )
                        );

        player.cancelRoundEntry();

        table.updateCountdownState();

        return PrivateTableMapper.toStateResponse(table);
    }

    @PostMapping("/{tableId}/ready")
    public String readyPlayer(
            @PathVariable String tableId
    ) {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        PrivateTable table =
                tableManager.getTable(tableId);

        TablePlayer player =
                table.getPlayers()
                        .stream()
                        .filter(p ->
                                p.getUsername()
                                        .equals(username)
                        )
                        .findFirst()
                        .orElseThrow();

        player.setReady(true);

        table.updateCountdownState();

        return "Player ready";
    }

    @PostMapping("/{tableId}/action")
    public PrivateTableStateResponse playAction(
            @PathVariable String tableId,
            @RequestBody TableActionRequest request
    ) {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        PrivateTable table =
                tableManager.getTable(tableId);

        table.updateCountdownState();

        table.autoStartRound();
        if (!table.isRoundStarted()) {

            throw new RuntimeException(
                    "Round not started"
            );
        }
        TablePlayer currentPlayer =
                table.getCurrentPlayer();

        if (!currentPlayer
                .getUsername()
                .equals(username)) {

            throw new RuntimeException(
                    "Not your turn"
            );
        }

        boolean roundFinished = false;

        switch (request.getAction()) {

            case "HIT":

                currentPlayer.getHand()
                        .addCard(
                                table.getShoe()
                                        .drawCard()
                        );

                if (currentPlayer.getHand().isBust()
                        || currentPlayer.getHand().isTwentyOne()) {

                    table.nextTurn();
                }

                break;

            case "STAND":

                table.nextTurn();

                break;

            case "DOUBLE":

                if (!currentPlayer.getHand().canDouble()) {

                    throw new RuntimeException(
                            "Cannot double after hit"
                    );
                }

                currentPlayer.doubleBet();

                currentPlayer.getHand()
                        .addCard(
                                table.getShoe()
                                        .drawCard()
                        );

                table.nextTurn();

                break;

            case "SPLIT":

                currentPlayer.splitHand(table.getShoe());

                if (!currentPlayer.hasPlayableHand()) {
                    table.nextTurn();
                }

                break;

            default:

                throw new RuntimeException(
                        "Invalid action"
                );
        }

        if (table.allPlayersFinished()) {

            table.playDealerTurnIfNeeded();

            table.resolveRound();

            roundFinished = true;
        }

        PrivateTableStateResponse response =
                PrivateTableMapper.toStateResponse(table);

        if (roundFinished) {
            privateTableHistoryService.saveIfNeeded(table);
        }

        return response;
    }

    @GetMapping("/reconnect")
    public PrivateTableResponse reconnect() {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        PrivateTable table =
                tableManager.findPlayerTable(
                        username
                );

        if (table == null) {

            throw new RuntimeException(
                    "Player is not in a table"
            );
        }

        return new PrivateTableResponse(

                table.getTableId(),

                table.getHostUsername(),

                table.getPlayers()
                        .stream()
                        .map(TablePlayer::getUsername)
                        .toList()
        );
    }
}
