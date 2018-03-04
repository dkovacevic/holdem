package com.wire.bots.holdem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
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
    boolean bot = false;
    @JsonIgnore
    Hand bestHand = null;
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
    private String role = "";
    @JsonProperty
    private int call;

    public Player() {
    }

    public Player(String userId, String name, ArrayList<Card> board) {
        this.id = userId;
        this.name = name;
        this.board = board;
    }

    @JsonIgnore
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

    @JsonIgnore
    @Nullable
    public Hand getBestHand() {
        if (bestHand == null) {
            Collection<Hand> allHands = getAllHands();
            bestHand = allHands.stream().max(Comparator.naturalOrder()).orElse(null);
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

    int take() {
        int ret = call;
        chips -= call;
        call = 0;
        return ret;
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

    String getRole() {
        return role;
    }

    void setRole(String role) {
        this.role = role;
    }

    int getCall() {
        return call;
    }

    void setCall(int call) {
        this.call = call;
    }

    int raiseCall(int raise) {
        call += raise;
        return call;
    }

    String getNameWithRole() {
        return role.isEmpty() ? getName() : String.format("%s(%s)", getName(), getRole());
    }

    boolean isBot() {
        return bot;
    }

    Action action(Action cmd) {
        return cmd;
    }

    @JsonIgnore
    ArrayList<Card> getBoard() {
        return board;
    }

    void setBoard(ArrayList<Card> board) {
        this.board = board;
    }
}
