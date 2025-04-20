package com.example.casino.Remik;

public class RemikCard {
    final static String[] suits = {"Hearts", "Diamonds", "Spades", "Clubs"};
    final static int[] suitsSortValues = {0, 26, 13, 39};
    final static String[] ranks = {"Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King", "Ace"};
    final static int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 1};
    final static int[] sortValues = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 1};

    private String suit;
    private String rank;
    private int value;
    private int sortValue;

    public RemikCard(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
        this.value = calculateValue(rank);
        this.sortValue = calculateSortValue(rank, suit);
    }

    private int calculateValue(String rank) {
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i].equals(rank)) {
                return values[i];
            }
        }
        return -1;
    }

    private int calculateSortValue(String rank, String suit) {
        int sortValue = 0;
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i].equals(rank)) {
                sortValue += sortValues[i];
            }
        }

        for (int i = 0; i < suits.length; i++) {
            if (suits[i].equals(suit)) {
                sortValue += suitsSortValues[i];
            }
        }

        return sortValue;
    }

    @Override
    public String toString() {
        return "Card{" +
                "suit='" + suit + '\'' +
                ", rank='" + rank + '\'' +
                ", value=" + value +
                ", sortValue=" + sortValue +
                '}';
    }

    public int getSortValue() {
        return sortValue;
    }

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    public int getValue() {
        return value;
    }
}
