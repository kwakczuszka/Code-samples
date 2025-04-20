package com.example.casino.Server;

public enum Kolor {
    spade, club, heart, diamond, Joker;

    public static Kolor getKolor(int i) {
        return values()[i];
    }
}
