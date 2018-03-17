package com.wire.bots.holdem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class Hand implements Comparable<Hand> {
    private ArrayList<Card> cards;

    Hand(Collection<Card> cards) {
        this.cards = new ArrayList<>(cards);
        this.cards.sort(Comparator.reverseOrder());
    }

    @Override
    public String toString() {
        if (straightFlush() != -1)
            return String.format("straight flush %s high", Card.rankAsString(straightFlush()));
        if (fourKind() != -1)
            return String.format("four of kind %s", Card.rankAsString(fourKind()));
        if (fullHouse() != -1)
            return String.format("full house of %s", Card.rankAsString(fullHouse()));
        if (flush() != -1)
            return String.format("flush %s high", Card.rankAsString(flush()));
        if (straight() != -1)
            return String.format("straight %s high", Card.rankAsString(straight()));
        if (threeKind() != -1)
            return String.format("three of kind %s", Card.rankAsString(threeKind()));
        if (twoPair() != -1)
            return String.format("two pairs %s high", Card.rankAsString(twoPair()));
        if (onePair() != -1)
            return String.format("one pair of %s's", Card.rankAsString(onePair()));

        return String.format("high card of %s", Card.rankAsString(highCard().getRank()));
    }

    @Override
    public int compareTo(Hand second) {
        if (straightFlush() > second.straightFlush())
            return 1;
        if (straightFlush() < second.straightFlush())
            return -1;

        if (fourKind() > second.fourKind())
            return 1;
        if (fourKind() < second.fourKind())
            return -1;

        if (fullHouse() > second.fullHouse())
            return 1;
        if (fullHouse() < second.fullHouse())
            return -1;

        if (flush() > second.flush())
            return 1;
        if (flush() < second.flush())
            return -1;

        if (straight() > second.straight())
            return 1;
        if (straight() < second.straight())
            return -1;

        if (threeKind() > second.threeKind())
            return 1;
        if (threeKind() < second.threeKind())
            return -1;

        if (twoPair() > second.twoPair())
            return 1;
        if (twoPair() < second.twoPair())
            return -1;

        if (secondPair() > second.secondPair())
            return 1;
        if (secondPair() < second.secondPair())
            return -1;

        if (onePair() > second.onePair())
            return 1;
        if (onePair() < second.onePair())
            return -1;

        return higherCard(second);
    }

    private int higherCard(Hand that) {
        if (cards.get(0).getRank() > that.cards.get(0).getRank())
            return 1;
        if (cards.get(0).getRank() < that.cards.get(0).getRank())
            return -1;

        if (cards.get(1).getRank() > that.cards.get(1).getRank())
            return 1;
        if (cards.get(1).getRank() < that.cards.get(1).getRank())
            return -1;

        if (cards.size() <= 2 || that.cards.size() <= 2)
            return 0;

        if (cards.get(2).getRank() > that.cards.get(2).getRank())
            return 1;
        if (cards.get(2).getRank() < that.cards.get(2).getRank())
            return -1;

        if (cards.get(3).getRank() > that.cards.get(3).getRank())
            return 1;
        if (cards.get(3).getRank() < that.cards.get(3).getRank())
            return -1;

        if (cards.get(4).getRank() > that.cards.get(4).getRank())
            return 1;
        if (cards.get(4).getRank() < that.cards.get(4).getRank())
            return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        int ret = 0;
        for (Card card : cards) {
            ret += card.hashCode() + (ret + 250);
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Hand) {
            Hand other = (Hand) o;
            if (cards.size() != other.cards.size())
                return false;
            for (int i = 0; i < cards.size(); i++) {
                if (!cards.get(i).equals(other.cards.get(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    ArrayList<Card> getCards() {
        return cards;
    }

    int straightFlush() {
        return straight() != -1 && flush() != -1 ? highCard().getRank() : -1;
    }

    int flush() {
        if (cards.size() != 5)
            return -1;
        boolean flush = cards.stream().allMatch(card -> card.getSuit() == highCard().getSuit());
        return flush ? highCard().getRank() : -1;
    }

    int straight() {
        if (cards.size() != 5)
            return -1;

        for (int i = 1; i < cards.size() - 1; i++) {
            int prev = cards.get(i - 1).getRank();
            int mid = cards.get(i).getRank();
            int next = cards.get(i + 1).getRank();

            if (i == 1 && cards.get(0).getRank() == 12 && cards.get(4).getRank() == 0)
                continue;// Ace and Two at the beginning

            if (mid - 1 != next || mid + 1 != prev)
                return -1;
        }
        return cards.get(1).getRank() == 3 ? cards.get(1).getRank() : highCard().getRank();
    }


    int fourKind() {
        for (int i = 0; i < cards.size() - 3; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            int c3 = cards.get(i + 2).getRank();
            int c4 = cards.get(i + 3).getRank();
            if (c1 == c2 && c2 == c3 && c3 == c4)
                return c4;
        }
        return -1;
    }

    int fullHouse() {
        int ret = threeKind();
        if (ret == -1)
            return -1;

        for (int i = 0; i < cards.size() - 1; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            if (c1 != ret && c1 == c2) {
                return ret;
            }
        }
        return -1;
    }

    int threeKind() {
        for (int i = 0; i < cards.size() - 2; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            int c3 = cards.get(i + 2).getRank();
            if (c1 == c2 && c2 == c3)
                return c3;
        }
        return -1;
    }

    int twoPair() {
        return pairs(2, 0);
    }

    public int onePair() {
        return pairs(1, 0);
    }

    public int strongestCard() {
        return highCard().getRank();
    }

    private int secondPair() {
        return pairs(2, 1);
    }

    private Card highCard() {
        return cards.get(0);
    }

    private int pairs(int size, int ord) {
        ArrayList<Integer> pairs = new ArrayList<>();
        for (int i = 0; i < cards.size() - 1; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            if (c1 == c2) {
                i++; //to avoid three of kind
                pairs.add(c1);
            }
        }
        return pairs.size() == size ? pairs.get(ord) : -1;
    }

    public HandStrength getStrength() {
        if (straightFlush() != -1)
            return HandStrength.StraightFlush;
        if (fourKind() != -1)
            return HandStrength.FourOfKind;
        if (fullHouse() != -1)
            return HandStrength.FullHouse;
        if (flush() != -1)
            return HandStrength.Flush;
        if (straight() != -1)
            return HandStrength.Straight;
        if (threeKind() != -1)
            return HandStrength.ThreeOfKind;
        if (twoPair() != -1)
            return HandStrength.TwoPair;
        if (onePair() != -1)
            return HandStrength.OnePair;
        return HandStrength.HighCard;
    }

    boolean isSuited() {
        if (cards.isEmpty())
            return false;
        int suit = cards.get(0).getSuit();
        return cards.stream().allMatch(x -> x.getSuit() == suit);
    }
}


