package com.wire.bots.holdem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.holdem.strategies.*;

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
        //ret.add(new Hand(cards));
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
    public Hand getBestHand() {
        Collection<Hand> allHands = getAllHands();
        return allHands.stream().max(Comparator.naturalOrder()).orElse(new Hand(cards));
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

    /**
     * AI for the bot. Input param cmd is Action performed by the player that bet before
     *
     * @param cmd Previous action (action of some other Player)
     * @return Bot's call as the result to @cmd
     */
    Action action(Action cmd) {
        Strategy s = chooseStrategy();

        if (cmd == Action.DEAL) {
            // Bot is the Caller
            if (getRole() == Role.Caller)
                return s.action(cmd);
            else
                return Action.DEAL; //ignore
        }

        return s.action(cmd);
    }

    private Strategy chooseStrategy() {
        if (board.size() == 3)
            return new LoosePassive(this);
        if (chips < 50)
            return new TightPassive(this);
        if (call > 20)
            return new TightAggressive(this);

        return new LooseAggressive(this);
    }

    @JsonIgnore
    public ArrayList<Card> getBoard() {
        return board;
    }

    void setBoard(ArrayList<Card> board) {
        this.board = board;
    }
}
