package com.bl21.dto.response;

import java.util.List;

public class PrivateTableResponse {

    private String tableId;

    private String hostUsername;

    private List<String> players;

    public PrivateTableResponse(
            String tableId,
            String hostUsername,
            List<String> players
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

    public List<String> getPlayers() {
        return players;
    }
}
