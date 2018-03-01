package com.wire.bots.holdem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

public class Player implements Comparable<Player> {
    private static final int INITIAL_CHIPS = 100;
    private final String id;
    private final String name;
    private final ArrayList<Card> cards = new ArrayList<>();
    private final ArrayList<Card> board;
    private Hand bestHand = null;
    private int chips = INITIAL_CHIPS;
    private boolean called;
    private boolean folded;

    public Player(String userId, String name, ArrayList<Card> board) {
        this.id = userId;
        this.name = name;
        this.board = board;
    }

    public Collection<Hand> getAllHands() {
        HashSet<Hand> ret = new HashSet<>();
        int n = board.size();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if (i != j && j != k && i != k) {
                        ArrayList<Card> tmp = new ArrayList<>();
                        tmp.add(cards.get(0));
                        tmp.add(cards.get(1));
                        tmp.add(board.get(i));
                        tmp.add(board.get(j));
                        tmp.add(board.get(k));

                        ret.add(new Hand(tmp));
                    }
                }
            }
        }
        return ret;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public Hand getBestHand() {
        if (bestHand == null) {
            Collection<Hand> allHands = getAllHands();
            bestHand = allHands.stream().max(Comparator.naturalOrder()).get();
        }
        return bestHand;
    }

    String getId() {
        return id;
    }

    ArrayList<Card> getCards() {
        return cards;
    }

    @Override
    public int compareTo(Player o) {
        return getBestHand().compareTo(o.getBestHand());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Player && hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getName() {
        return name;
    }

    void reset() {
        bestHand = null;
        cards.clear();
        called = false;
        folded = false;
    }

    int take(int val) {
        chips -= val;
        return val;
    }

    void put(int val) {
        chips += val;
    }

    int getChips() {
        return chips;
    }

    boolean isCalled() {
        return called || folded;
    }

    void setCalled(boolean val) {
        this.called = val;
    }

    void fold() {
        folded = true;
    }

    boolean isFolded() {
        return folded;
    }
}
