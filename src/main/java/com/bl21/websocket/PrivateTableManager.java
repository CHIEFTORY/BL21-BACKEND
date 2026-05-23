package com.bl21.websocket;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PrivateTableManager {

    private final Map<String, PrivateTable> tables =
            new HashMap<>();

    public PrivateTable createTable(
            String hostUsername,
            Long buyIn
    ) {

        String tableId =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 6);

        PrivateTable table =
                new PrivateTable(
                        tableId,
                        hostUsername,
                        buyIn
                );

        tables.put(tableId, table);

        return table;
    }

    public PrivateTable getTable(
            String tableId
    ) {

        PrivateTable table =
                tables.get(tableId);

        if (table == null) {

            throw new RuntimeException(
                    "Table not found"
            );
        }

        return table;
    }
    public PrivateTable joinTable(
            String tableId,
            String username
    ) {

        PrivateTable table =
                getTable(tableId);

        table.getPlayers().add(
                new TablePlayer(
                        username,
                        table.getBuyIn()
                )
        );

        return table;
    }

    public void removeTable(
            String tableId
    ) {

        tables.remove(tableId);
    }

    public PrivateTable findPlayerTable(
            String username
    ) {

        return tables.values()

                .stream()

                .filter(table ->

                        table.getPlayers()

                                .stream()

                                .anyMatch(player ->

                                        player.getUsername()
                                                .equals(username)
                                )
                )

                .findFirst()

                .orElse(null);
    }
}