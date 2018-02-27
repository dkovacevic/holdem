package com.wire.bots.holdem;

import com.wire.bots.sdk.server.model.User;

import java.util.ArrayList;
import java.util.Comparator;

public class Table {
    private final ArrayList<Player> players = new ArrayList<>();
    private final ArrayList<Card> board = new ArrayList<>();
    private Deck deck;

    public Table(Deck deck) {
        this.deck = deck;
    }

    public Player addPlayer(User user) {
        Player player = new Player(user.id, user.name, board);
        players.add(player);
        return player;
    }

    public ArrayList<Card> addCardToBoard() {
        board.add(deck.drawFromDeck());
        return board;
    }

    public Player getWinner() {
        return players.stream().max(Comparator.naturalOrder()).get();
    }

    public ArrayList<Card> getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void foldPlayer(String userId) {
        players.forEach(player -> {
            if (player.getUserId().equals(userId))
                player.setFolded(true);
        });
    }

    public void removePlayer(String userId) {
        players.removeIf(player -> player.getUserId().equals(userId));
    }

    public Card dealCard(Player player) {
        Card card = deck.drawFromDeck();
        player.addCard(card);
        return card;
    }

    public void shuffle() {
        deck = new Deck();
        board.clear();
        players.forEach(player -> player.setFolded(false));
    }
}
