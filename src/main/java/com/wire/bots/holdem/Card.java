package com.wire.bots.holdem;

public class Card implements Comparable<Card> {
    private static String[] suits = {"hearts", "spades", "diamonds", "clubs"};
    private static String[] ranks = {"ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king"};
    private int rank, suit;

    public Card(int suit, int rank) {
        this.rank = rank;
        this.suit = suit;
    }

    static String rankAsString(int rank) {
        return ranks[rank];
    }

    @Override
    public String toString() {
        return ranks[rank] + "_of_" + suits[suit];
    }

    int getRank() {
        return rank;
    }

    int getSuit() {
        return suit;
    }

    @Override
    public int compareTo(Card o) {
        if (rank > o.rank)
            return 1;
        if (rank < o.rank)
            return -1;

        if (suit > o.suit)
            return 1;
        if (suit < o.suit)
            return -1;

        return 0; //the same cards
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Card && hashCode() == o.hashCode();
    }
}