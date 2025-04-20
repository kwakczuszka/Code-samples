package com.example.casino.Server;

import java.util.ArrayList;
import java.util.Collections;

public class Talia {
    private ArrayList<Karta> karty;

    public void SzuflujTalie() {
        Collections.shuffle(karty);
    }

    public Karta KartaZTalii() {
        return karty.remove(karty.size() - 1);
    }

    public void KartaDoTalii(Karta k) {
        karty.add(k);
    }

    public Talia(boolean czyJokery) {
        this.karty = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 13; j++) {
                karty.add(new Karta(i, j));
            }
        }
        if (czyJokery) {
            for (int i = 0; i < 3; i++) {
                karty.add(new Karta(4, 13));
            }
        }
    }
}

