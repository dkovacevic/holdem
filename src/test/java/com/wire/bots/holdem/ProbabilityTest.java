package com.wire.bots.holdem;

import org.junit.Test;

import java.util.ArrayList;

public class ProbabilityTest {
    @Test
    public void holeCards() {
        Probability probability = new Probability(new ArrayList<>(), new ArrayList<>());

        int combinations = probability.combinations();
        assert combinations == 1326;
    }

    @Test
    public void flopTest() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 0));
        board.add(new Card(2, 6));
        board.add(new Card(1, 8));

        Player p1 = new Player("1", "1", board);
        p1.addCard(new Card(1, 7));
        p1.addCard(new Card(0, 2));
        Probability probability1 = new Probability(board, p1.getCards());
        
        Player p2 = new Player("2", "2", board);
        p2.addCard(new Card(2, 0));
        p2.addCard(new Card(2, 1));
        Probability probability2 = new Probability(board, p2.getCards());

        float chance1 = probability1.chance(p1);
        float chance2 = probability2.chance(p2);

        assert chance1 < chance2;
    }

    @Test
    public void turnTest() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(2, 10));
        board.add(new Card(1, 9));
        board.add(new Card(0, 9));

        Player p1 = new Player("1", "1", board);
        p1.addCard(new Card(1, 0));
        p1.addCard(new Card(0, 1));
        Probability probability1 = new Probability(board, p1.getCards());

        Player p2 = new Player("2", "2", board);
        p2.addCard(new Card(2, 0));
        p2.addCard(new Card(2, 7));
        Probability probability2 = new Probability(board, p2.getCards());

        float chance1 = probability1.chance(p1);
        float chance2 = probability2.chance(p2);

        assert chance1 < chance2;
    }

    @Test
    public void riverTest() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(1, 12));
        board.add(new Card(2, 10));
        board.add(new Card(1, 9));
        board.add(new Card(0, 9));
        board.add(new Card(0, 4));

        Player p1 = new Player("1", "1", board);
        p1.addCard(new Card(1, 0));
        p1.addCard(new Card(0, 1));
        Probability probability1 = new Probability(board, p1.getCards());

        Player p2 = new Player("2", "2", board);
        p2.addCard(new Card(2, 0));
        p2.addCard(new Card(2, 7));
        Probability probability2 = new Probability(board, p2.getCards());

        float chance1 = probability1.chance(p1);
        float chance2 = probability2.chance(p2);

        assert chance1 < chance2;
    }
}
