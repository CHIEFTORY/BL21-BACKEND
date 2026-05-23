package com.bl21.dto.response;

import com.bl21.websocket.TablePlayer;

import java.util.List;
import java.util.Set;

public class PrivateTableResponse {

    private String tableId;

    private String hostUsername;

    private List<TablePlayer> players;

    public PrivateTableResponse(
            String tableId,
            String hostUsername,
            List<TablePlayer> players
    ) {

        this.tableId = tableId;
        this.hostUsername = hostUsername;
        this.players = players;
    }

    public String getTableId() {
        return tableId;
    }

    public String getHostUsername() {
        return hostUsername;
    }

    public List<TablePlayer> getPlayers() {
        return players;
    }
}