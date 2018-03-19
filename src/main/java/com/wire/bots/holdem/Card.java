package com.wire.bots.holdem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Card implements Comparable<Card> {
    private static final String[] suits = {"hearts", "spades", "diamonds", "clubs"};
    private static final String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king", "ace"};
    @JsonProperty
    private int rank;
    @JsonProperty
    private int suit;

    public Card() {
    }

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

    public int getRank() {
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
        return (rank * 4) + suit;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Card && hashCode() == o.hashCode();
    }
}