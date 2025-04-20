package com.example.casino.Remik;

import java.util.Collections;
import java.util.LinkedList;

public class RemikDeck {
    LinkedList<RemikCard> cardsInDeck = new LinkedList<>();
    LinkedList<RemikCard> discardedCards = new LinkedList<>();

    public RemikDeck() {
        discardedCards = new LinkedList<>();
        for (int i = 0; i < 2; i++) {  // bo 2 talie
            for (String suit : RemikCard.suits) {
                for (String rank : RemikCard.ranks) {
                    RemikCard temp = new RemikCard(suit, rank);
                    cardsInDeck.add(temp);
                }
            }
        }
        Collections.shuffle(cardsInDeck);
    }

    public RemikCard dealOne() {
        if (!cardsInDeck.isEmpty()) {
            return cardsInDeck.remove(0);
        } else {
            return null;
        }
    }

    public int cardsLeftInDeck() {
        return cardsInDeck.size();
    }

    public void discard(RemikCard card) {
        discardedCards.add(card);
    }

    public void refillDeckFromDiescardedCards() {
        cardsInDeck.addAll(discardedCards);
        discardedCards.clear();
        Collections.shuffle(cardsInDeck);
    }

    public boolean isDeckEmpty() {
        return cardsInDeck.isEmpty();
    }

    public LinkedList<RemikCard> getDiscardedCards() {
        return discardedCards;
    }
}


