package com.wire.bots.holdem;

import java.util.ArrayList;

public class Hand implements Comparable<Hand> {
    private ArrayList<Card> cards;
    private int[] value;

    public Hand(ArrayList<Card> cards) {
        value = new int[6];
        this.cards = cards;

        int[] ranks = new int[14];
        int[] orderedRanks = new int[5];     //miscellaneous cards that are not otherwise significant
        boolean flush = true, straight = false;
        int sameCards = 1, sameCards2 = 1;
        int largeGroupRank = 0, smallGroupRank = 0;
        int index = 0;
        int topStraightValue = 0;

        for (int x = 0; x <= 13; x++) {
            ranks[x] = 0;
        }
        for (int x = 0; x <= 4; x++) {
            ranks[cards.get(x).getRank()]++;
        }
        for (int x = 0; x < 4; x++) {
            if (cards.get(x).getSuit() != cards.get(x + 1).getSuit())
                flush = false;
        }

        for (int x = 13; x >= 1; x--) {
            if (ranks[x] > sameCards) {
                if (sameCards != 1) {  //if sameCards was not the default value
                    sameCards2 = sameCards;
                    smallGroupRank = x;   //changed from smallGroupRank=largeGroupRank;
                }

                sameCards = ranks[x];
                largeGroupRank = x;
            } else if (ranks[x] > sameCards2) {
                sameCards2 = ranks[x];
                smallGroupRank = x;
            }
        }

        //if ace, run this before because ace is highest card
        if (ranks[1] == 1) {
            orderedRanks[index] = 14;
            index++;
        }

        for (int x = 13; x >= 2; x--) {
            if (ranks[x] == 1) {
                orderedRanks[index] = x; //if ace
                index++;
            }
        }

        //can't have straight with lowest value of more than 10
        for (int x = 1; x <= 9; x++) {
            if (ranks[x] == 1 && ranks[x + 1] == 1 && ranks[x + 2] == 1 && ranks[x + 3] == 1 && ranks[x + 4] == 1) {
                straight = true;
                topStraightValue = x + 4; //4 above bottom value
                break;
            }
        }

        //ace high
        if (ranks[10] == 1 && ranks[11] == 1 && ranks[12] == 1 && ranks[13] == 1 && ranks[1] == 1) {
            straight = true;
            topStraightValue = 14; //higher than king
        }

        for (int x = 0; x <= 5; x++) {
            value[x] = 0;
        }

        //start hand evaluation
        if (sameCards == 1) {
            value[0] = 1;
            value[1] = orderedRanks[0];
            value[2] = orderedRanks[1];
            value[3] = orderedRanks[2];
            value[4] = orderedRanks[3];
            value[5] = orderedRanks[4];
        }

        if (sameCards == 2 && sameCards2 == 1) {
            value[0] = 2;
            value[1] = largeGroupRank; //rank of pair
            value[2] = orderedRanks[0];
            value[3] = orderedRanks[1];
            value[4] = orderedRanks[2];
        }

        //two pair
        if (sameCards == 2 && sameCards2 == 2) {
            value[0] = 3;
            value[1] = largeGroupRank > smallGroupRank ? largeGroupRank : smallGroupRank; //rank of greater pair
            value[2] = largeGroupRank < smallGroupRank ? largeGroupRank : smallGroupRank;
            value[3] = orderedRanks[0];  //extra card
        }

        if (sameCards == 3 && sameCards2 != 2) {
            value[0] = 4;
            value[1] = largeGroupRank;
            value[2] = orderedRanks[0];
            value[3] = orderedRanks[1];
        }

        if (straight && !flush) {
            value[0] = 5;
            value[1] = topStraightValue;
        }

        if (flush && !straight) {
            value[0] = 6;
            value[1] = orderedRanks[0]; //tie determined by ranks of cards
            value[2] = orderedRanks[1];
            value[3] = orderedRanks[2];
            value[4] = orderedRanks[3];
            value[5] = orderedRanks[4];
        }

        if (sameCards == 3 && sameCards2 == 2) {
            value[0] = 7;
            value[1] = largeGroupRank;
            value[2] = smallGroupRank;
        }

        if (sameCards == 4) {
            value[0] = 8;
            value[1] = largeGroupRank;
            value[2] = orderedRanks[0];
        }

        if (straight && flush) {
            value[0] = 9;
            value[1] = topStraightValue;
        }
    }

    public String display() {
        String s;
        switch (value[0]) {

            case 1:
                s = "High card";
                break;
            case 2:
                s = "Pair of " + Card.rankAsString(value[1]) + "\'s";
                break;
            case 3:
                s = "Two pair " + Card.rankAsString(value[1]) + " " + Card.rankAsString(value[2]);
                break;
            case 4:
                s = "Three of a kind " + Card.rankAsString(value[1]) + "\'s";
                break;
            case 5:
                s = Card.rankAsString(value[1]) + " high straight";
                break;
            case 6:
                s = "Flush";
                break;
            case 7:
                s = "Full house " + Card.rankAsString(value[1]) + " over " + Card.rankAsString(value[2]);
                break;
            case 8:
                s = "Four of a kind " + Card.rankAsString(value[1]);
                break;
            case 9:
                s = "Straight flush " + Card.rankAsString(value[1]) + " high";
                break;
            default:
                s = "Error in Hand.display: value[0] contains invalid value";
        }
        return s;
    }

    @Override
    public int compareTo(Hand that) {
        for (int x = 0; x < 6; x++) {
            if (this.value[x] > that.value[x])
                return 1;
            else if (this.value[x] < that.value[x])
                return -1;
        }
        return 0; //if hands are equal
    }

    public ArrayList<Card> getCards() {
        return cards;
    }
}


