package com.wire.bots.holdem;

import java.util.ArrayList;
import java.util.Random;

public class Deck {
    private ArrayList<Card> cards;
    private final static Random generator = new Random();

    Deck() {
        cards = new ArrayList<>();

        for (short a = 0; a <= 3; a++) {
            for (short b = 0; b <= 12; b++) {
                cards.add(new Card(a, b));
            }
        }

        int size = cards.size() - 1;

        for (short i = 0; i < 1000; i++) {
            int index1 = generator.nextInt(size);
            int index2 = generator.nextInt(size);

            Card temp = cards.get(index2);
            cards.set(index2, cards.get(index1));
            cards.set(index1, temp);
        }
    }

    Card drawFromDeck() {
        return cards.remove(cards.size() - 1);
    }

    public int getTotalCards() {
        return cards.size();  //we could use this method when making a complete poker game to see if we needed a new deck
    }
}