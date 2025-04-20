package com.example.casino.Server;


import java.util.Map;

import static java.util.Map.entry;

public enum Rank {
    Two, Three, Four, Five, Six, Seven, Eight, Nine, Teen, Jack, Queen, King, Ace, Joker;

    public static Rank getRank(int i) {
        return values()[i];
    }

    public static Map<String, String> rank = Map.ofEntries(
            entry("Two", "2"),
            entry("Three", "3"),
            entry("Four", "4"),
            entry("Five", "5"),
            entry("Six", "6"),
            entry("Seven", "7"),
            entry("Eight", "8"),
            entry("Nine", "9"),
            entry("Teen", "10"),
            entry("Jack", "j"),
            entry("Queen", "q"),
            entry("King", "k"),
            entry("Ace", "a")
    );

    public static int getIntValue(Rank r) {
        return r.ordinal();
    }
}
