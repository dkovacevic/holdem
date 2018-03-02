package com.wire.bots.holdem;

import com.wire.bots.sdk.server.model.User;

import java.util.*;
import java.util.stream.Collectors;

class Table {
    private static final int INITIAL_SMALL_BLIND = 1;
    private static final int INITIAL_RAISE = 5;
    private static final int BLIND_INCREASE = 3;
    private static final int RAISE_INCREASE = 10;
    private final HashMap<String, Player> players = new HashMap<>();
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
        return players
                .values()
                .stream()
                .filter(player -> !player.isFolded())
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
        return players.values().stream().filter(player -> !player.isFolded()).collect(Collectors.toList());
    }

    boolean isAllFolded() {
        return getActivePlayers().size() <= 1;
    }

    boolean fold(String userId) {
        Player player = getPlayer(userId);
        if (!player.isCalled()) {
            player.fold();
            return true;
        }
        return false;
    }

    void removePlayer(String userId) {
        players.remove(userId);
    }

    Card dealCard(Player player) {
        Card card = deck.drawFromDeck();
        player.addCard(card);
        return card;
    }

    void shuffle() {
        roundNumber++;
        if (roundNumber % 3 == 0)
            smallBlind += BLIND_INCREASE;
        if (roundNumber % 3 == 0)
            raise += RAISE_INCREASE;
        deck = new Deck();
        board.clear();
        players.values().forEach(Player::reset);
        newBet();
    }

    // Pay to the Player and flush the pot
    int flushPot(Player player) {
        int ret = pot;
        player.put(pot);
        pot = 0;
        return ret;
    }

    boolean call(String userId) {
        Player player = getPlayer(userId);
        if (!player.isCalled()) {
            pot += player.take(bet);
            player.setCalled(true);
            return true;
        }
        return false;
    }

    void blind(String userId) {
        Player player = getPlayer(userId);
        pot += player.take(bet);
    }

    int raise(String userId) {
        Player player = getPlayer(userId);
        if (!player.isCalled()) {
            bet += raise;
            resetCallers();
            call(userId);
            return bet;
        }
        return -1;
    }

    private void resetCallers() {
        players.values().forEach(player -> player.setCalled(false));
    }

    void newBet() {
        resetCallers();
        bet = smallBlind;
    }

    boolean isAllCalled() {
        return players.values().stream().allMatch(Player::isCalled);
    }

    boolean isShowdown() {
        return board.size() == 5;
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

    Collection<Player> collectPlayers() {
        ArrayList<Player> ret = new ArrayList<>();
        Iterator<Map.Entry<String, Player>> iterator = players.entrySet().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next().getValue();
            if (player.getChips() <= 0) {
                iterator.remove();
                ret.add(player);
            }
        }
        return ret;
    }

}
