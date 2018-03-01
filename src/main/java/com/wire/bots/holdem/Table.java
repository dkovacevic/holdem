package com.wire.bots.holdem;

import com.wire.bots.sdk.server.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

class Table {
    private static final int INITIAL_SMALL_BLIND = 1;
    private static final int INITIAL_RAISE = 5;
    private final HashMap<String, Player> players = new HashMap<>();
    private final HashMap<String, Player> round = new HashMap<>();
    private final ArrayList<Card> board = new ArrayList<>();
    private Deck deck;
    private int pot;
    private int bet;
    private int roundNumber;
    private int raise = INITIAL_RAISE;
    private int smallBlind = INITIAL_SMALL_BLIND;

    Table(Deck deck) {
        this.deck = deck;
    }

    Player addPlayer(User user) {
        Player player = new Player(user.id, user.name, board);
        players.put(player.getId(), player);
        return player;
    }

    ArrayList<Card> flopCard() {
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
        roundNumber++;
        if (roundNumber % 3 == 0)
            smallBlind += smallBlind;
        if (roundNumber % 5 == 0)
            raise += raise;
        deck = new Deck();
        board.clear();
        players.values().forEach(Player::reset);
        round.clear();
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

    int raise(String userId) {
        bet += raise;
        resetCallers();
        call(userId);
        return bet;
    }

    private void resetCallers() {
        round.values().forEach(player -> player.setCalled(false));
    }

    void newBet() {
        resetCallers();
        bet = smallBlind;
    }

    boolean isAllCalled() {
        return round.values().stream().allMatch(Player::isCalled);
    }

    boolean isShowdown() {
        return board.size() == 5;
    }

    boolean isDone() {
        return isFlopped() && round.size() <= 1;
    }

    boolean isAllPlaying() {
        return round.size() == players.size() && isAllCalled();
    }

    boolean isFlopped() {
        return !board.isEmpty();
    }

    int getRoundNumber() {
        return roundNumber;
    }

    int getPot() {
        return pot;
    }

    int getSmallBlind() {
        return smallBlind;
    }

    int getRaise() {
        return raise;
    }

    Player getPlayer(String userId) {
        return players.get(userId);
    }
}
