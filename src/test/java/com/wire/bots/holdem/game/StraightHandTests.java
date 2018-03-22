package com.wire.bots.holdem.game;

import org.junit.Test;

import java.util.ArrayList;

public class StraightHandTests {
    @Test
    public void straightFiveOverThree() {
        ArrayList<Card> cards1 = new ArrayList<>();
        cards1.add(new Card(1, 1));
        cards1.add(new Card(2, 2));
        cards1.add(new Card(1, 3));
        cards1.add(new Card(1, 4));
        cards1.add(new Card(0, 5));

        Hand h1 = new Hand(cards1);

        ArrayList<Card> cards2 = new ArrayList<>();
        cards2.add(new Card(1, 12));
        cards2.add(new Card(2, 12));
        cards2.add(new Card(3, 12));
        cards2.add(new Card(1, 10));
        cards2.add(new Card(1, 9));

        Hand h2 = new Hand(cards2);

        int res = h1.compareTo(h2);
        assert res == 1;
    }

    @Test
    public void straightAceOverThree() {
        ArrayList<Card> cards1 = new ArrayList<>();
        cards1.add(new Card(0, 12));
        cards1.add(new Card(2, 11));
        cards1.add(new Card(1, 10));
        cards1.add(new Card(1, 9));
        cards1.add(new Card(0, 8));

        Hand h1 = new Hand(cards1);

        ArrayList<Card> cards2 = new ArrayList<>();
        cards2.add(new Card(1, 12));
        cards2.add(new Card(2, 12));
        cards2.add(new Card(3, 12));
        cards2.add(new Card(0, 10));
        cards2.add(new Card(0, 9));

        Hand h2 = new Hand(cards2);

        int res = h1.compareTo(h2);
        assert res == 1;
    }
}
