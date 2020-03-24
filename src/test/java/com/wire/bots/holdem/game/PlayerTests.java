package com.wire.bots.holdem.game;

import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerTests {
    @Test
    public void royalOverAces() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(0, 2));
        board.add(new Card(1, 10));
        board.add(new Card(1, 9));
        board.add(new Card(1, 8));

        Player p1 = new Player(UUID.randomUUID(), "1", board);
        p1.addCard(new Card(1, 11));
        p1.addCard(new Card(0, 8));

        Player p2 = new Player(UUID.randomUUID(), "2", board);
        p2.addCard(new Card(2, 12));
        p2.addCard(new Card(3, 12));

        assert p1.compareTo(p2) > 0;
        assert p2.compareTo(p1) < 0;
    }

    @Test
    public void strongerHoleCardsHands() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(1, 11));
        board.add(new Card(1, 10));
        board.add(new Card(1, 9));
        board.add(new Card(1, 8));

        Player p1 = new Player(UUID.randomUUID(), "1", board);
        p1.addCard(new Card(2, 2));
        p1.addCard(new Card(3, 2));

        Player p2 = new Player(UUID.randomUUID(), "2", board);
        p2.addCard(new Card(2, 12));
        p2.addCard(new Card(3, 11));

        assert p1.compareTo(p2) > 0;
        assert p2.compareTo(p1) < 0;
    }

    @Test
    public void absolutelyEqualHands() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(1, 11));
        board.add(new Card(1, 10));
        board.add(new Card(1, 9));
        board.add(new Card(1, 8));

        Player p1 = new Player(UUID.randomUUID(), "1", board);
        p1.addCard(new Card(0, 2));
        p1.addCard(new Card(0, 3));

        Player p2 = new Player(UUID.randomUUID(), "2", board);
        p2.addCard(new Card(2, 2));
        p2.addCard(new Card(3, 3));

        assert p1.compareTo(p2) > 0;
        assert p2.compareTo(p1) < 0;
    }

    @Test
    public void drawHands() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(1, 11));
        board.add(new Card(1, 10));
        board.add(new Card(1, 9));
        board.add(new Card(1, 8));

        Player p1 = new Player(UUID.randomUUID(), "1", board);
        p1.addCard(new Card(0, 2));
        p1.addCard(new Card(1, 3));

        Player p2 = new Player(UUID.randomUUID(), "2", board);
        p2.addCard(new Card(2, 2));
        p2.addCard(new Card(3, 3));

        assert p1.compareTo(p2) == 0;
        assert p2.compareTo(p1) == 0;
    }

    @Test
    public void flopTest() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(1, 11));
        board.add(new Card(1, 10));

        Player p1 = new Player(UUID.randomUUID(), "1", board);
        p1.addCard(new Card(0, 9));
        p1.addCard(new Card(1, 8));

        Player p2 = new Player(UUID.randomUUID(), "2", board);
        p2.addCard(new Card(2, 2));
        p2.addCard(new Card(3, 3));

        assert p1.compareTo(p2) > 0;
        assert p2.compareTo(p1) < 0;
    }

    @Test
    public void turnTest() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(1, 11));
        board.add(new Card(1, 10));
        board.add(new Card(1, 9));

        Player p1 = new Player(UUID.randomUUID(), "1", board);
        p1.addCard(new Card(0, 2));
        p1.addCard(new Card(1, 8));

        Player p2 = new Player(UUID.randomUUID(), "2", board);
        p2.addCard(new Card(2, 12));
        p2.addCard(new Card(3, 9));

        assert p1.compareTo(p2) > 0;
        assert p2.compareTo(p1) < 0;
    }
}
