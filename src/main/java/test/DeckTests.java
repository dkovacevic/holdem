package test;

import com.wire.bots.holdem.Card;
import com.wire.bots.holdem.Deck;

import java.io.IOException;

public class DeckTests {
    public static void main(String[] args) throws IOException {
        Deck deck = new Deck();
        while (deck.size() > 0) {
            Card card = deck.drawFromDeck();
            System.out.println(card);
        }
        System.out.println();
        deck = new Deck();
        while (deck.size() > 0) {
            Card card = deck.drawFromDeck();
            System.out.println(card);
        }
    }
}
