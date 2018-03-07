package com.wire.bots.holdem;

import org.junit.Test;

import java.util.ArrayList;

public class PlayerTests {
    @Test
    public void royalOverAces() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(0, 2));
        board.add(new Card(1, 10));
        board.add(new Card(1, 9));
        board.add(new Card(1, 8));

        Player p1 = new Player("1", "1", board);
        p1.addCard(new Card(1, 11));
        p1.addCard(new Card(0, 8));

        Player p2 = new Player("2", "2", board);
        p1.addCard(new Card(2, 12));
        p1.addCard(new Card(3, 12));

        assert p1.compareTo(p2) == 1;
        assert p2.compareTo(p1) == -1;
    }

    @Test
    public void equalHands() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(1, 11));
        board.add(new Card(1, 10));
        board.add(new Card(1, 9));
        board.add(new Card(1, 8));

        Player p1 = new Player("1", "1", board);
        p1.addCard(new Card(2, 2));
        p1.addCard(new Card(3, 2));

        Player p2 = new Player("2", "2", board);
        p1.addCard(new Card(2, 12));
        p1.addCard(new Card(3, 11));

        assert p1.compareTo(p2) == 1;
        assert p2.compareTo(p1) == -1;
    }

    @Test
    public void absolutelyEqualHands() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(1, 11));
        board.add(new Card(1, 10));
        board.add(new Card(1, 9));
        board.add(new Card(1, 8));

        Player p1 = new Player("1", "1", board);
        p1.addCard(new Card(0, 2));
        p1.addCard(new Card(0, 3));

        Player p2 = new Player("2", "2", board);
        p1.addCard(new Card(2, 2));
        p1.addCard(new Card(3, 3));

        assert p1.compareTo(p2) == 1;
        assert p2.compareTo(p1) == -1;
    }
}
