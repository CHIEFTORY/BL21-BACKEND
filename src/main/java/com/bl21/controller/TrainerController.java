package com.bl21.controller;

import com.bl21.blackjack.engine.BlackjackEngine;
import com.bl21.dto.response.GameStateResponse;
import com.bl21.dto.request.TrainerMoveRequest;
import com.bl21.dto.response.StrategyAdviceResponse;
import com.bl21.dto.response.TrainerMistakeResponse;
import com.bl21.dto.response.TrainerMoveResponse;
import com.bl21.entity.Card;
import com.bl21.entity.Hand;
import com.bl21.enums.MoveAction;
import com.bl21.mapper.GameMapper;
import com.bl21.mapper.ShoeMapper;
import com.bl21.service.GameManager;
import com.bl21.service.TrainerService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trainer")
public class TrainerController {

    private final GameManager gameManager;

    private final TrainerService trainerService;

    public TrainerController(
            GameManager gameManager,
            TrainerService trainerService
    ) {

        this.gameManager = gameManager;
        this.trainerService = trainerService;
    }

    @PostMapping("/start")
    public GameStateResponse startTrainerHand(
            @RequestParam(defaultValue = "all") String mode
    ) {

        String gameId =
                gameManager.createTrainerGame(
                        getCurrentUsername(),
                        mode
                );

        BlackjackEngine game =
                gameManager.getGame(gameId);

        return new GameStateResponse(
                gameId,
                game.getPlayerHands()
                        .stream()
                        .map(GameMapper::toHandResponse)
                        .toList(),
                GameMapper.toDealerHandResponse(
                        game.getDealerHand(),
                        true
                ),
                game.getGameStatus().name(),
                100L,
                null,
                null,
                null,
                game.getCurrentHandIndex(),
                List.of(100L),
                ShoeMapper.toResponse(game.getShoe())
        );
    }

    @PostMapping("/{gameId}/move")
    public TrainerMoveResponse validateMove(
            @PathVariable String gameId,
            @RequestBody TrainerMoveRequest request
    ) {

        BlackjackEngine game = gameManager.getGame(gameId);

        Hand playerHand = game.getCurrentHand();

        Card dealerCard =
                game.getDealerHand().getCards().get(0);

        MoveAction playerMove =
                MoveAction.valueOf(request.getMove());

        boolean correct =
                trainerService.validateMove(
                        playerHand,
                        dealerCard,
                        playerMove
                );

        MoveAction correctMove =
                trainerService.getCorrectMove(
                        playerHand,
                        dealerCard
                );

        return new TrainerMoveResponse(
                correct,
                playerMove.name(),
                correctMove.name()
        );
    }

    @GetMapping("/{gameId}/strategy")
    public StrategyAdviceResponse getStrategyAdvice(
            @PathVariable String gameId
    ) {

        BlackjackEngine game = gameManager.getGame(gameId);

        Hand playerHand = game.getCurrentHand();

        Card dealerCard =
                game.getDealerHand().getCards().get(0);

        return trainerService.getStrategyAdvice(
                playerHand,
                dealerCard
        );
    }

    @GetMapping("/mistakes")
    public List<TrainerMistakeResponse> getFrequentMistakes() {

        return trainerService.getFrequentMistakes();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}
