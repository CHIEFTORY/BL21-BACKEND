package com.bl21.websocket;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PrivateTableManager {

    private final Map<String, PrivateTable> tables =
            new ConcurrentHashMap<>();

    public synchronized PrivateTable createTable(
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
    public synchronized PrivateTable joinTable(
            String tableId,
            String username
    ) {

        PrivateTable table =
                getTable(tableId);

        table.addPlayer(username);

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
