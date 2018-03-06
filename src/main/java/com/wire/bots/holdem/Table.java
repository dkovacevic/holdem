package com.wire.bots.holdem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.sdk.server.model.User;

import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
class Table {
    private static final int INITIAL_SMALL_BLIND = 1;
    private static final int INITIAL_RAISE = 5;
    private static final int BLIND_INCREASE = 2;
    private static final int RAISE_INCREASE = 5;
    @JsonProperty
    private final ArrayList<Player> players = new ArrayList<>();
    @JsonProperty
    private final ArrayList<Card> board = new ArrayList<>();
    @JsonProperty
    private Deck deck;
    @JsonProperty
    private int pot;
    @JsonProperty
    private int roundNumber;
    @JsonProperty
    private int raise = INITIAL_RAISE;
    @JsonProperty
    private int smallBlind = INITIAL_SMALL_BLIND;

    public Table() {
    }

    Table(Deck deck) {
        this.deck = deck;
    }

    Player addPlayer(User user, boolean bot) {
        return addPlayer(user.id, user.name, bot);
    }

    Player addPlayer(String userId, String name, boolean bot) {
        Player player = new Player(userId, name, board);
        player.setBot(bot);

        players.add(player);

        if (players.size() == 1)
            player.setRole(Role.SB);
        if (players.size() == 2)
            player.setRole(Role.BB);
        if (players.size() == 3)
            player.setRole(Role.Caller);

        return player;
    }

    void flopCard() {
        board.add(deck.drawFromDeck());
    }

    @JsonIgnore
    Player getWinner() {
        return players
                .stream()
                .filter(player -> !player.isFolded())
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    ArrayList<Card> getBoard() {
        return board;
    }

    Collection<Player> getPlayers() {
        return players;
    }

    Collection<Player> getActivePlayers() {
        return players.stream().filter(player -> !player.isFolded()).collect(Collectors.toList());
    }

    Collection<Player> getTopPlayers() {
        return players.stream().sorted((o1, o2) -> Integer.compare(o2.getChips(), o1.getChips())).collect(Collectors.toList());
    }

    Collection<Player> getFoldedPlayers() {
        return players.stream().filter(Player::isFolded).collect(Collectors.toList());
    }

    boolean isAllFolded() {
        return getActivePlayers().size() <= 1;
    }

    void removePlayer(String userId) {
        players.removeIf(player -> player.getId().equals(userId));
    }

    Card dealCard(Player player) {
        Card card = deck.drawFromDeck();
        player.addCard(card);
        return card;
    }

    void shuffle() {
        roundNumber++;
        pot = 0;
        if (roundNumber % 3 == 0)
            smallBlind += BLIND_INCREASE;
        if (roundNumber % 5 == 0)
            raise += RAISE_INCREASE;
        deck = new Deck();
        board.clear();
        players.forEach(Player::reset);
    }

    void shiftRoles() {
        if (players.size() <= 1)
            return;

        if (players.size() == 2) {
            Role tmp = players.get(0).getRole();
            players.get(0).setRole(players.get(1).getRole());
            players.get(1).setRole(tmp);
            return;
        }

        ListIterator<Player> iterator = players.listIterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.getRole() == Role.Caller)
                player.setRole(Role.Player);

            if (player.getRole() == Role.SB) {
                player.setRole(Role.Caller);

                if (iterator.hasNext()) {
                    Player next = iterator.next();
                    next.setRole(Role.SB);
                    if (iterator.hasNext()) {
                        next = iterator.next();
                        next.setRole(Role.BB);
                    } else {
                        players.get(0).setRole(Role.BB);
                    }
                } else {
                    players.get(0).setRole(Role.SB);
                    players.get(1).setRole(Role.BB);
                }

                return;
            }
        }
    }

    // Pay to the Player and flush the pot
    int flushPot(Player player) {
        synchronized (players) {
            return refund(player, pot);
        }
    }

    private int refund(Player player, int refund) {
        pot -= refund;
        player.put(refund);
        return refund;
    }

    boolean blind(Player player) {
        switch (player.getRole()) {
            case SB: {
                player.setCall(smallBlind);
                int take = player.take();
                int ret = player.getCall();
                pot += take;
                player.setCall(smallBlind);
                return ret == 0;
            }
            case BB: {
                player.setCall(2 * smallBlind);
                int take = player.take();
                int ret = player.getCall();
                pot += take;
                return ret == 0;
            }
            default:
                player.setCall(2 * smallBlind);
                return true;
        }
    }

    int raise(Player player) {
        synchronized (players) {
            if (!player.isCalled() && player.getChips() > 0) {
                int raise = this.raise <= player.getChips() ? this.raise : player.getChips();
                raiseCallers(raise);
                call(player);
                return raise;
            }
            return -1;
        }
    }

    int call(Player player) {
        synchronized (players) {
            if (!player.isCalled() && player.getChips() >= 0) {
                int take = player.take();
                pot += take;
                player.setCalled(player.getCall() == 0);
                return take;
            }
            return -1;
        }
    }

    boolean fold(Player player) {
        synchronized (players) {
            if (!player.isCalled()) {
                player.fold();
                return true;
            }
            return false;
        }
    }

    private void raiseCallers(int raise) {
        synchronized (players) {
            players.forEach(player -> {
                player.raiseCall(raise);
                player.setCalled(false);
            });
        }
    }

    void newBet() {
        players.forEach(player -> {
            player.setCall(0);
            player.setCalled(false);
        });
    }

    boolean isAllCalled() {
        synchronized (players) {
            return players.stream().allMatch(Player::isCalled);
        }
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
        synchronized (players) {
            return pot;
        }
    }

    int getSmallBlind() {
        return smallBlind;
    }

    int getRaise() {
        return raise;
    }

    public void setRaise(int raise) {
        this.raise = raise;
    }

    Player getPlayer(String userId) {
        return players.stream()
                .filter(player -> player.getId().equals(userId))
                .findAny()
                .orElse(null);
    }

    Collection<Player> collectPlayers() {
        List<Player> losers = players.stream().filter(player -> player.getChips() <= 0).collect(Collectors.toList());
        players.removeAll(losers);
        return losers;
    }

    String printPlayers() {
        StringBuilder sb = new StringBuilder();
        getPlayers().forEach(player -> sb.append(player.getNameWithRole()).append(" | "));
        return sb.toString();
    }

    void refund(int refund) {
        players.forEach(player -> {
            if (player.isCalled() && !player.isFolded()) {
                refund(player, refund);
            }
        });
    }

    boolean isSomeoneKaputt() {
        synchronized (players) {
            return players.stream().anyMatch(player -> player.getChips() <= 0);
        }
    }
}
