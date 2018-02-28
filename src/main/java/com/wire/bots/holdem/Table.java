package com.wire.bots.holdem;

import com.wire.bots.sdk.server.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

class Table {
    private static final int BLIND = 1;
    private final HashMap<String, Player> players = new HashMap<>();
    private final HashMap<String, Player> round = new HashMap<>();
    private final ArrayList<Card> board = new ArrayList<>();
    private Deck deck;
    private int pot;
    private int bet = BLIND;
    private boolean flopped;

    Table(Deck deck) {
        this.deck = deck;
    }

    Player addPlayer(User user) {
        Player player = new Player(user.id, user.name, board);
        players.put(player.getId(), player);
        return player;
    }

    ArrayList<Card> flopCard() {
        flopped = true;
        board.add(deck.drawFromDeck());
        return board;
    }

    Player getWinner() {
        return round
                .values()
                .stream()
                .max(Comparator.naturalOrder())
                .get();
    }

    ArrayList<Card> getBoard() {
        return board;
    }

    Collection<Player> getPlayers() {
        return players.values();
    }

    Collection<Player> getActivePlayers() {
        return round.values();
    }

    void fold(String userId) {
        round.remove(userId);
    }

    void removePlayer(String userId) {
        players.remove(userId);
        round.remove(userId);
    }

    Card dealCard(Player player) {
        Card card = deck.drawFromDeck();
        player.addCard(card);
        return card;
    }

    void shuffle() {
        deck = new Deck();
        board.clear();
        players.values().forEach(Player::reset);
        round.clear();
        flopped = false;
        newBet();
    }

    // Pay to the Player and flush the pot
    int flushPot(Player player) {
        int ret = pot;
        player.put(pot);
        pot = 0;
        return ret;
    }

    void call(String userId) {
        Player player = players.get(userId);
        pot += player.take(bet);
        player.setCalled(true);
        round.put(player.getId(), player);
    }

    void blind(String userId) {
        Player player = players.get(userId);
        pot += player.take(bet);
    }

    void raise(String userId, int raise) {
        if (raise > 0) {
            bet += raise;
            resetCallers();
            call(userId);
        }
    }

    private void resetCallers() {
        round.values().forEach(player -> player.setCalled(false));
    }

    void newBet() {
        resetCallers();
        bet = BLIND;
    }

    boolean isAllCalled() {
        return flopped && round.values().stream().anyMatch(Player::isCalled);
    }

    boolean isShowdown() {
        return flopped && board.size() == 5;
    }

    boolean isDone() {
        return flopped && round.size() <= 1;
    }
}
