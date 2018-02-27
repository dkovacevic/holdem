package com.wire.bots.holdem;

public class Card implements Comparable<Card> {
    private short rank, suit;

    private static String[] suits = {"hearts", "spades", "diamonds", "clubs"};
    private static String[] ranks = {"ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king"};

    static String rankAsString(int rank) {
        return ranks[rank];
    }

    public Card(short suit, short rank) {
        this.rank = rank;
        this.suit = suit;
    }

    @Override
    public String toString() {
        return ranks[rank] + "_of_" + suits[suit];
    }

    short getRank() {
        return rank;
    }

    short getSuit() {
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
}