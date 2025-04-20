package com.example.casino.Server;

import java.io.Serializable;

public class Karta implements Serializable {
    public Kolor kolor;
    public Rank rank;

    //public Image skin;
    public Karta(int kolor, int rank) {
        this.kolor = Kolor.getKolor(kolor);
        this.rank = Rank.getRank(rank);
        //this.skin = new Image("src/main/resources/cards/"+rank+kolor+".png");
    }
}
