package com.example.casino;

import com.example.casino.Server.Karta;
import com.example.casino.Server.Kolor;
import com.example.casino.Server.PokerHand;
import com.example.casino.Server.Rank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Player implements Serializable {
    private String username;
    private Integer id;
    private boolean isReady;
    private Karta card1;
    private Karta card2;
    public PokerHand pokerHand;
    public boolean passedAway;
    public Integer money;
    public Integer curBid;


    public Player(Integer id, String username, boolean isReady) {
        this.id = id;
        this.username = username;
        this.isReady = isReady;
        this.money = 1000;
        this.pokerHand = new PokerHand();
        this.curBid = 0;
    }

    public void clearHand() {
        this.card1 = null;
        this.card2 = null;
        this.pokerHand.clearHand();
    }

    public String getPlayerData() {
        return username;
    }

    public Integer getPlayerID() {
        return id;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setCard1(Karta card1) {
        this.card1 = card1;
    }

    public void setCard2(Karta card2) {
        this.card2 = card2;
        this.pokerHand.addHand(new ArrayList<>(List.of(new Karta[]{this.card1, this.card2})));
    }

    public Karta getCard1() {
        return card1;
    }

    public Karta getCard2() {
        return card2;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getMoney() {
        return money;
    }

    public void pass() {
        this.passedAway = true;
    }
}
