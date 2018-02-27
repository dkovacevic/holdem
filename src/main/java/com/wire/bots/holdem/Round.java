package com.wire.bots.holdem;

import java.util.ArrayList;
import java.util.Comparator;

public class Round {
    private final ArrayList<Player> players = new ArrayList<>();
    private final ArrayList<Card> board = new ArrayList<>();

    public Player addPlayer(String userId) {
        Player player = new Player(userId, board);
        players.add(player);
        return player;
    }

    public void addCardToBoard(Card card) {
        board.add(card);
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
        players.removeIf(player -> player.getUserId().equals(userId));
    }
}
