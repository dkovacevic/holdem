package com.wire.bots.holdem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.holdem.strategies.*;

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
    private boolean bot = false;
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
        Collection<Hand> allHands = getAllHands();
        return allHands.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    String getId() {
        return id;
    }

    ArrayList<Card> getCards() {
        return cards;
    }

    @Override
    public int compareTo(Player o) {
        Hand bestHand1 = getBestHand();
        Hand bestHand2 = o.getBestHand();
        if (bestHand2 == null)
            return 1;
        if (bestHand1 == null)
            return -1;
        return bestHand1.compareTo(bestHand2);
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

    Role getRole() {
        return role;
    }

    void setRole(Role role) {
        this.role = role;
    }

    int getCall() {
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

    /**
     * AI for the bot. Input param cmd is Action performed by the player that bet before
     *
     * @param cmd Previous action (action of some other Player)
     * @return Bot's call as the result to @cmd
     */
    Action action(Action cmd) {
        Hand hand = getBestHand();
        Strategy s = chooseStrategy();

        if (cmd == Action.DEAL) {
            // Bot is the Caller
            if (getRole() == Role.Caller)
                return s.action(hand, getCall());
            else
                return Action.DEAL; //ignore
        }

        return s.action(hand, getCall());
    }

    private Strategy chooseStrategy() {
        if (board.size() == 3)
            return new LoosePassive();
        if (chips < 50)
            return new TightPassive();
        if (call > 20)
            return new TightAggressive();
        return new LooseAggressive();
    }

    @JsonIgnore
    private ArrayList<Card> getBoard() {
        return board;
    }

    void setBoard(ArrayList<Card> board) {
        this.board = board;
    }
}
