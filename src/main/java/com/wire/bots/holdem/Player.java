package com.wire.bots.holdem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Comparator;

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
    @JsonProperty
    private boolean turn;
    @JsonIgnore
    private Hand bestHand;

    public Player() {
    }

    public Player(String userId, String name, ArrayList<Card> board) {
        this.id = userId;
        this.name = name;
        this.board = board;
    }

    private static ArrayList<Hand> allHands(ArrayList<Card> cards) {
        ArrayList<Hand> ret = new ArrayList<>();
        for (int i = 0; i < (1 << cards.size()); i++) {
            ArrayList<Card> sub = new ArrayList<>();
            for (int j = 0; j < cards.size(); j++) {
                if ((i & (1 << j)) > 0) {
                    Card card = cards.get(j);
                    sub.add(card);
                }
            }
            if (sub.size() == 5)
                ret.add(new Hand(sub));
        }
        return ret;
    }

    @Override
    public String toString() {
        return getNameWithRole();
    }

    void addCard(Card card) {
        cards.add(card);
    }

    @JsonIgnore
    public Hand getBestHand() {
        if (bestHand == null) {
            ArrayList<Card> all = new ArrayList<>();
            all.addAll(cards);
            all.addAll(board);

            bestHand = allHands(all).stream().max(Comparator.naturalOrder()).orElse(new Hand(cards));
        }
        return bestHand;
    }

    String getId() {
        return id;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    @Override
    public int compareTo(Player other) {
        Hand bestHand1 = getBestHand();
        Hand bestHand2 = other.getBestHand();
        int res = bestHand1.compareTo(bestHand2);
        if (res != 0)
            return res;

        Hand h1 = new Hand(cards);
        Hand h2 = new Hand(other.cards);
        res = h1.compareTo(h2);
        if (res != 0)
            return res;

        if (h1.isSuited() && !h2.isSuited())
            return 1;
        if (!h1.isSuited() && h2.isSuited())
            return -1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Player && id.equalsIgnoreCase(((Player) o).getId());
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
        turn = false;
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
        String ret = role == Role.Player ? getName() : String.format("%s(%s)", name, role);
        return turn ? String.format("**%s**", ret) : ret;
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

    boolean isTurn() {
        return turn;
    }

    void setTurn(boolean turn) {
        this.turn = turn;
    }

    void resetBestHand() {
        bestHand = null;
    }
}
