package com.bl21.controller;


import com.bl21.dto.request.TableActionRequest;
import com.bl21.dto.response.PrivateTableStateResponse;
import com.bl21.mapper.PrivateTableMapper;
import com.bl21.websocket.PrivateTable;
import com.bl21.websocket.PrivateTableManager;
import com.bl21.websocket.TablePlayer;
import com.bl21.service.PrivateTableHistoryService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class PrivateTableSocketController {

    private final PrivateTableManager tableManager;

    private final SimpMessagingTemplate messagingTemplate;
    private final PrivateTableHistoryService privateTableHistoryService;

    public PrivateTableSocketController(
            PrivateTableManager tableManager,
            SimpMessagingTemplate messagingTemplate,
            PrivateTableHistoryService privateTableHistoryService
    ) {

        this.tableManager = tableManager;
        this.messagingTemplate = messagingTemplate;
        this.privateTableHistoryService = privateTableHistoryService;
    }

    @MessageMapping("/table.action")
    public void handleAction(
            TableActionRequest request
    ) {


        PrivateTable table =
                tableManager.getTable(
                        request.getTableId()
                );
        table.updateCountdownState();
        table.autoStartRound();
        if (!table.isRoundStarted()) {

            throw new RuntimeException(
                    "Round not started"
            );
        }
        TablePlayer currentPlayer =
                table.getCurrentPlayer();

        String username =
                request.getUsername();

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
        }

        if (table.allPlayersFinished()) {

            table.playDealerTurnIfNeeded();

            table.resolveRound();

            roundFinished = true;
        }

        PrivateTableStateResponse response =
                PrivateTableMapper.toStateResponse(table);

        messagingTemplate.convertAndSend(

                "/topic/table/" + table.getTableId(),

                response
        );

        if (roundFinished) {
            privateTableHistoryService.saveIfNeeded(table);
        }
    }
}
