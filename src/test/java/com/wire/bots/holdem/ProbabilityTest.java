package com.wire.bots.holdem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class ProbabilityTest {
    @Test
    public void holeCards() {
        ArrayList<Card> holeCards = new ArrayList<>();
        ArrayList<Card> board = new ArrayList<>();
        Probability probability = new Probability(board, holeCards);

        int combinations = probability.combinations();
        assert combinations == 1326;

        holeCards.add(new Card(1, 4));
        holeCards.add(new Card(2, 6));
        probability = new Probability(board, holeCards);

        combinations = probability.combinations();
        assert combinations == 1225;

        board.add(new Card(1, 11));
        board.add(new Card(2, 12));
        board.add(new Card(2, 8));
        probability = new Probability(board, holeCards);

        combinations = probability.combinations();
        assert combinations == 1081;

        board.add(new Card(0, 2));
        probability = new Probability(board, holeCards);

        combinations = probability.combinations();
        assert combinations == 1035;

        board.add(new Card(3, 4));
        probability = new Probability(board, holeCards);

        combinations = probability.combinations();
        assert combinations == 990;
    }

    @Test
    public void flopTest() {
        ArrayList<Card> board = new ArrayList<>();
        board.add(new Card(2, 12));
        board.add(new Card(2, 2));
        board.add(new Card(1, 9));

        Player p1 = new Player("1", "1", board);
        p1.addCard(new Card(1, 12));
        p1.addCard(new Card(1, 8));
        Probability probability1 = new Probability(board, p1.getCards());

        Player p2 = new Player("2", "2", board);
        p2.addCard(new Card(2, 9));
        p2.addCard(new Card(3, 8));
        Probability probability2 = new Probability(board, p2.getCards());

        float chance1 = probability1.chance(p1);
        float chance2 = probability2.chance(p2);

        assert chance1 > chance2;
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

        Date s = new Date();
        p1.getBestHand();
        Date e = new Date();
        System.out.printf("profiler getBestHand: %d\n", e.getTime() - s.getTime());

        float chance1 = probability1.chance(p1);
        float chance2 = probability2.chance(p2);

        assert chance1 < chance2;
    }
}
