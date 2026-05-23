package com.bl21.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_moves")
public class PlayerMove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerHand;

    private String dealerCard;

    private String playerMove;

    @Column(name = "action")
    private String action;

    private String correctMove;

    private Boolean correct;

    @Column(name = "move_number")
    private Integer moveNumber;

    private LocalDateTime createdAt =
            LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public PlayerMove() {
    }

    public Long getId() {
        return id;
    }

    public String getPlayerHand() {
        return playerHand;
    }

    public void setPlayerHand(String playerHand) {
        this.playerHand = playerHand;
    }

    public String getDealerCard() {
        return dealerCard;
    }

    public void setDealerCard(String dealerCard) {
        this.dealerCard = dealerCard;
    }

    public String getPlayerMove() {
        return playerMove;
    }

    public void setPlayerMove(String playerMove) {
        this.playerMove = playerMove;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCorrectMove() {
        return correctMove;
    }

    public void setCorrectMove(String correctMove) {
        this.correctMove = correctMove;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Integer getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(Integer moveNumber) {
        this.moveNumber = moveNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
