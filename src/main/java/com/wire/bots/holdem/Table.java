package com.wire.bots.holdem;

import com.wire.bots.sdk.server.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

public class Table {
    private static final int BLIND = 1;
    private final HashMap<String, Player> players = new HashMap<>();
    private final ArrayList<Card> board = new ArrayList<>();
    private Deck deck;
    private int pot;
    private int bet = BLIND;

    public Table(Deck deck) {
        this.deck = deck;
    }

    public Player addPlayer(User user) {
        Player player = new Player(user.id, user.name, board);
        players.put(player.getUserId(), player);
        return player;
    }

    public ArrayList<Card> flopCard() {
        board.add(deck.drawFromDeck());
        return board;
    }

    public Player getWinner() {
        return players
                .values()
                .stream()
                .filter(Player::isActive)
                .max(Comparator.naturalOrder())
                .get();
    }

    public ArrayList<Card> getBoard() {
        return board;
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    public void fold(String userId) {
        players.get(userId).fold();
    }

    public void removePlayer(String userId) {
        players.remove(userId);
    }

    public Card dealCard(Player player) {
        Card card = deck.drawFromDeck();
        player.addCard(card);
        return card;
    }

    public void shuffle() {
        deck = new Deck();
        board.clear();
        players.values().forEach(Player::reset);
        resetBet();
    }

    // Pay to the Player and flush the pot
    public int flushPot(Player player) {
        int ret = pot;
        player.put(pot);
        pot = 0;
        return ret;
    }

    public int getPot() {
        return pot;
    }

    public void call(String userId) {
        Player player = players.get(userId);
        pot += player.take(bet);
        player.setActive();
    }

    public void blind(String userId) {
        Player player = players.get(userId);
        pot += player.take(bet);
    }

    public void raise(String userId, int raise) {
        bet += raise;
        call(userId);
    }

    public void resetBet() {
        bet = BLIND;
    }
}
