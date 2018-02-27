package com.wire.bots.holdem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

public class Player implements Comparable<Player> {
    private final String userId;
    private final ArrayList<Card> cards = new ArrayList<>();
    private final ArrayList<Card> board;
    private Hand bestHand = null;

    public Player(String userId, ArrayList<Card> board) {
        this.userId = userId;
        this.board = board;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public Hand bestHand() {
        if (bestHand == null) {
            Collection<Hand> allHands = getAllHands(cards.get(0), cards.get(1), board);
            bestHand = allHands.stream().max(Comparator.naturalOrder()).get();
        }
        return bestHand;
    }

    private static Collection<Hand> getAllHands(Card c1, Card c2, ArrayList<Card> cards) {
        HashSet<Hand> ret = new HashSet<>();
        int n = cards.size();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if (i != j && j != k && i != k) {
                        ArrayList<Card> tmp = new ArrayList<>();
                        tmp.add(c1);
                        tmp.add(c2);
                        tmp.add(cards.get(i));
                        tmp.add(cards.get(j));
                        tmp.add(cards.get(k));

                        ret.add(new Hand(tmp));
                    }
                }
            }
        }
        return ret;
    }

    public String getUserId() {
        return userId;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    @Override
    public int compareTo(Player o) {
        return bestHand().compareTo(o.bestHand());
    }
}
