package com.wire.bots.holdem;

import org.junit.Test;

import java.util.ArrayList;

public class PlayerTests {
    @Test
    public void pairRoyalOverAces() {
        ArrayList<Card> cards1 = new ArrayList<>();
        cards1.add(new Card(1, 12));
        cards1.add(new Card(0, 2));
        cards1.add(new Card(1, 10));
        cards1.add(new Card(1, 9));
        cards1.add(new Card(1, 8));

        Player p1 = new Player("1", "1", cards1);
        p1.addCard(new Card(1, 11));
        p1.addCard(new Card(0, 8));

        Player p2 = new Player("2", "2", cards1);
        p1.addCard(new Card(2, 12));
        p1.addCard(new Card(3, 12));

        int res = p1.compareTo(p2);
        assert res == 1;
    }

    @Test
    public void equalHands() {
        ArrayList<Card> cards1 = new ArrayList<>();
        cards1.add(new Card(1, 12));
        cards1.add(new Card(1, 11));
        cards1.add(new Card(1, 10));
        cards1.add(new Card(1, 9));
        cards1.add(new Card(1, 8));

        Player p1 = new Player("1", "1", cards1);
        p1.addCard(new Card(2, 2));
        p1.addCard(new Card(3, 2));

        Player p2 = new Player("2", "2", cards1);
        p1.addCard(new Card(2, 12));
        p1.addCard(new Card(3, 11));

        int res = p1.compareTo(p2);
        assert res == 1;
    }
}
