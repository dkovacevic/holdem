package com.wire.bots.holdem;

import org.junit.Test;

import java.util.ArrayList;

public class RankTests {
    @Test
    public void onePair() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 12));
        cards.add(new Card(2, 12));
        cards.add(new Card(1, 1));
        cards.add(new Card(1, 2));
        cards.add(new Card(1, 3));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == -1;
        assert h.onePair() == 12;
    }

    @Test
    public void twoPair() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 12));
        cards.add(new Card(2, 10));
        cards.add(new Card(1, 10));
        cards.add(new Card(1, 0));
        cards.add(new Card(0, 0));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == 10;
        assert h.onePair() == -1;
    }

    @Test
    public void threeOfKind() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 7));
        cards.add(new Card(2, 7));
        cards.add(new Card(0, 7));
        cards.add(new Card(1, 1));
        cards.add(new Card(0, 0));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == -1;
        assert h.threeKind() == 7;
        assert h.twoPair() == -1;
    }

    @Test
    public void fourOfKind() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 0));
        cards.add(new Card(2, 0));
        cards.add(new Card(0, 0));
        cards.add(new Card(3, 0));
        cards.add(new Card(0, 12));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == 0;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == -1;
        assert h.onePair() == -1;
    }

    @Test
    public void flush() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 5));
        cards.add(new Card(1, 8));
        cards.add(new Card(1, 10));
        cards.add(new Card(1, 2));
        cards.add(new Card(1, 1));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == 10;
        assert h.straight() == -1;
        assert h.twoPair() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == -1;
    }

    @Test
    public void straight() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 5));
        cards.add(new Card(2, 6));
        cards.add(new Card(1, 7));
        cards.add(new Card(0, 8));
        cards.add(new Card(1, 9));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == 9;
        assert h.twoPair() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == -1;
    }

    @Test
    public void straightAceBeginning() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 12));
        cards.add(new Card(2, 0));
        cards.add(new Card(1, 1));
        cards.add(new Card(0, 2));
        cards.add(new Card(1, 3));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == 3;
        assert h.twoPair() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == -1;
    }

    @Test
    public void straightAceEnding() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 12));
        cards.add(new Card(2, 11));
        cards.add(new Card(1, 10));
        cards.add(new Card(0, 9));
        cards.add(new Card(1, 8));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == 12;
        assert h.twoPair() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == -1;
    }

    @Test
    public void flushRoyal() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(3, 12));
        cards.add(new Card(3, 11));
        cards.add(new Card(3, 10));
        cards.add(new Card(3, 9));
        cards.add(new Card(3, 8));

        Hand h = new Hand(cards);

        assert h.straightFlush() == 12;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == 12;
        assert h.straight() == 12;
        assert h.twoPair() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == -1;
    }

    @Test
    public void highCard() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 11));
        cards.add(new Card(2, 9));
        cards.add(new Card(1, 7));
        cards.add(new Card(0, 5));
        cards.add(new Card(1, 3));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == -1;
        assert h.twoPair() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == -1;
        assert h.strongestCard() == 11;
    }

    @Test
    public void highCard2() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 12));
        cards.add(new Card(2, 11));
        cards.add(new Card(1, 10));
        cards.add(new Card(0, 9));
        cards.add(new Card(1, 7));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == -1;
        assert h.twoPair() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == -1;
        assert h.strongestCard() == 12;
    }

    @Test
    public void highCard3() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 12));
        cards.add(new Card(2, 0));
        cards.add(new Card(1, 1));
        cards.add(new Card(0, 2));
        cards.add(new Card(1, 4));

        Hand h = new Hand(cards);

        assert h.straightFlush() == -1;
        assert h.fourKind() == -1;
        assert h.fullHouse() == -1;
        assert h.flush() == -1;
        assert h.straight() == -1;
        assert h.twoPair() == -1;
        assert h.threeKind() == -1;
        assert h.twoPair() == -1;
        assert h.strongestCard() == 12;
    }
}
