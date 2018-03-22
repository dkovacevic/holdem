package com.wire.bots.holdem.game;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class Probability {
    private static final HashSet<Hand> hands = holeHands();
    private final ArrayList<Player> players = new ArrayList<>();

    public Probability(ArrayList<Card> board, ArrayList<Card> hole) {
        Date s = new Date();
        HashSet<Card> usedCards = new HashSet<>();
        usedCards.addAll(board);
        usedCards.addAll(hole);
        for (Hand h : hands) {
            Card c1 = h.getCard(0);
            Card c2 = h.getCard(1);

            if (usedCards.contains(c1) || usedCards.contains(c2))
                continue;

            Player player = new Player("", "", board);
            player.addCard(c1);
            player.addCard(c2);
            players.add(player);
            player.getBestHand();
        }
        Date e = new Date();
        //System.out.printf("profiler Probability: %d\n", e.getTime() - s.getTime());
    }

    private static ArrayList<Card> deck() {
        ArrayList<Card> ret = new ArrayList<>(52);
        for (int i = 0; i <= 12; i++) {
            for (int j = 0; j <= 3; j++) {
                Card card = new Card(j, i);
                ret.add(card);
            }
        }
        return ret;
    }

    private static HashSet<Hand> holeHands() {
        HashSet<Hand> hands = new HashSet<>(1326);
        for (Card c1 : deck()) {
            for (Card c2 : deck()) {
                if (!c1.equals(c2)) {
                    Hand h = new Hand(c1, c2);
                    hands.add(h);
                }
            }
        }
        return hands;
    }

    public float chance(Player player) {
        Date s = new Date();
        int w = 0;
        for (Player p : players) {
            w += player.compareTo(p) == 1 ? 1 : 0;
        }
        Date e = new Date();
        //System.out.printf("profiler chance: %d\n", e.getTime() - s.getTime());

        return (100f * w) / combinations();
    }

    int combinations() {
        return players.size();
    }
}
