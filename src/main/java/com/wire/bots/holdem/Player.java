package com.wire.bots.holdem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Player implements Comparable<Player> {
    private static final int INITIAL_CHIPS = 100;
    @JsonProperty
    private final ArrayList<Card> cards = new ArrayList<>();
    @JsonProperty
    protected boolean bot = false;
    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonIgnore
    private ArrayList<Card> board;
    @JsonProperty
    private int chips = INITIAL_CHIPS;
    @JsonProperty
    private boolean called;
    @JsonProperty
    private boolean folded;
    @JsonProperty
    private Role role = Role.Player;
    @JsonProperty
    private int call;

    public Player() {
    }

    public Player(String userId, String name, ArrayList<Card> board) {
        this.id = userId;
        this.name = name;
        this.board = board;
    }

    @Override
    public String toString() {
        return getNameWithRole();
    }

    @JsonIgnore
    private Collection<Hand> allHands() {
        HashSet<Hand> ret = new HashSet<>();
        ArrayList<Card> all = new ArrayList<>();
        all.addAll(cards);
        all.addAll(board);

        for (Card a : all) {
            for (Card b : all) {
                for (Card c : all) {
                    for (Card d : all) {
                        for (Card e : all) {
                            HashSet<Card> tmp = new HashSet<>();
                            tmp.add(a);
                            tmp.add(b);
                            tmp.add(c);
                            tmp.add(d);
                            tmp.add(e);

                            if (tmp.size() == 5)
                                ret.add(new Hand(tmp));
                        }
                    }
                }
            }
        }
        return ret;
    }

    void addCard(Card card) {
        cards.add(card);
    }

    @JsonIgnore
    public Hand getBestHand() {
        Collection<Hand> allHands = allHands();
        return allHands.stream().max(Comparator.naturalOrder()).orElse(new Hand(cards));
    }

    String getId() {
        return id;
    }

    ArrayList<Card> getCards() {
        return cards;
    }

    @Override
    public int compareTo(Player other) {
        Hand bestHand1 = getBestHand();
        Hand bestHand2 = other.getBestHand();
        int res = bestHand1.compareTo(bestHand2);
        if (res == 0) {
            Hand h1 = new Hand(cards);
            Hand h2 = new Hand(other.cards);
            res = h1.compareTo(h2);
            if (res == 0) {
                return cards.get(0).getSuit() == cards.get(1).getSuit() ? 1 : 0;
            } else
                return res;
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Player && hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    String getName() {
        return name;
    }

    void reset() {
        cards.clear();
        called = false;
        folded = false;
    }

    int take() {
        int ret = call <= chips ? call : chips;
        chips -= ret;
        call -= ret;
        return ret;
    }

    void put(int val) {
        chips += val;
    }

    public int getChips() {
        return chips;
    }

    void setChips(int chips) {
        this.chips = chips;
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

    Role getRole() {
        return role;
    }

    void setRole(Role role) {
        this.role = role;
    }

    public int getCall() {
        return call;
    }

    void setCall(int call) {
        this.call = call;
    }

    void raiseCall(int raise) {
        call += raise;
    }

    String getNameWithRole() {
        if (role == Role.SB || role == Role.BB)
            return String.format("%s(%s)", getName(), getRole());
        return role == Role.Caller ? String.format("**%s**", getName()) : getName();
    }

    boolean isBot() {
        return bot;
    }

    void setBot(boolean bot) {
        this.bot = bot;
    }

    @JsonIgnore
    public ArrayList<Card> getBoard() {
        return board;
    }

    void setBoard(ArrayList<Card> board) {
        this.board = board;
    }
}
