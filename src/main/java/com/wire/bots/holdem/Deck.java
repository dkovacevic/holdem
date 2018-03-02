package com.wire.bots.holdem;

import java.util.ArrayList;
import java.util.Random;

public class Deck {
    private final static Random rnd = new Random();
    private ArrayList<Card> cards;

    public Deck() {
        cards = new ArrayList<>(52);

        while (cards.size() < 52) {
            Card card = new Card(rnd.nextInt(4), rnd.nextInt(13));
            if (!cards.contains(card))
                cards.add(card);
        }
    }

    public Card drawFromDeck() {
        return cards.remove(cards.size() - 1);
    }

    public int size() {
        return cards.size();
    }
}