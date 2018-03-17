package com.wire.bots.holdem;

import java.util.ArrayList;
import java.util.HashSet;

class Probability {
    private final ArrayList<Player> players = new ArrayList<>();

    Probability(ArrayList<Card> board, ArrayList<Card> hole) {
        HashSet<Hand> hands = new HashSet<>(1326);
        HashSet<Card> used = new HashSet<>();
        used.addAll(board);
        used.addAll(hole);
        for (Card c1 : deck()) {
            if (used.contains(c1))
                continue;

            for (Card c2 : deck()) {
                if (used.contains(c2) || c1.equals(c2))
                    continue;

                ArrayList<Card> cards = new ArrayList<>(2);
                cards.add(c1);
                cards.add(c2);

                Hand h = new Hand(cards);
                if (hands.add(h)) {
                    String name = "" + h.hashCode();
                    Player player = new Player(name, name, board);
                    player.addCard(c1);
                    player.addCard(c2);
                    players.add(player);
                    player.getBestHand();
                }
            }
        }
    }

    float chance(Player player) {
        int w = 0;
        for (Player p : players) {
            w += player.compareTo(p) == 1 ? 1 : 0;
        }
        return (100f * w) / combinations();
    }

    private ArrayList<Card> deck() {
        ArrayList<Card> ret = new ArrayList<>(52);
        for (int i = 0; i <= 12; i++) {
            for (int j = 0; j <= 3; j++) {
                Card card = new Card(j, i);
                ret.add(card);
            }
        }
        return ret;
    }

    int combinations() {
        return players.size();
    }
}
