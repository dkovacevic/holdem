package com.wire.bots.holdem.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.Database;
import com.wire.bots.holdem.Images;
import com.wire.bots.holdem.Service;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.Member;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Poker {
    private static final ConcurrentHashMap<UUID, Table> tables = new ConcurrentHashMap<>();
    private static final String MIME_TYPE = "image/png";
    //private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(4);
    private final Ranking ranking;
    private final Database db;

    public Poker() {
        this.ranking = loadRanking();

        final Configuration.DB db = Service.CONFIG.db;
        this.db = new Database(db.host, db.port, db.password);
    }

    public void onRanking(WireClient client) throws Exception {
        client.sendText(ranking.print());
    }

    public void onAddBot(WireClient client) throws Exception {
        User user = new User();
        String name = Betman.randomName();
        user.id = UUID.randomUUID();
        user.name = name;
        addNewPlayer(client, user, true);
    }

    public void onFold(WireClient client, UUID userId) {
        Table table = getTable(client);
        Player player = table.getPlayer(userId);
        table.fold(player);
    }

    public void onCall(WireClient client, UUID userId) {
        Table table = getTable(client);
        Player player = table.getPlayer(userId);
        table.call(player);

        if (player.getCall() > 0) {
            table.refund(player.getCall());
        }
    }

    public void onRaise(WireClient client, UUID userId) throws Exception {
        Table table = getTable(client);
        Player player = table.getPlayer(userId);
        int raise = table.raise(player);
        if (raise != -1) {
            client.sendText(String.format("%s(%d) raised by %d, pot %d",
                    player.getName(),
                    player.getChips(),
                    raise,
                    table.getPot()));
        } else if (!player.isCalled()) {
            client.sendDirectText("Raise failed due to insufficient funds", player.getId());
        }
    }

    public void onDeal(WireClient client) throws Exception {
        Table table = getTable(client);
        if (table == null) {
            client.sendText("Type: `new` to start new game");
            return;
        }

        if (table.getPlayers().size() <= 1) {
            client.sendText("You are single player on this table. " +
                    "Add a participant to this conversation or type: `add bot` to add a bot player");
            return;
        }

        Player turn = table.shiftRoles();
        table.shuffle();
        turn.setTurn(true);
        Player player = table.turn();

        client.sendText(String.format("%d. %s blind %d - raise %d",
                table.getRoundNumber(),
                table.printPlayers(),
                table.getSmallBlind(),
                table.getRaise()));

        dealPlayers(client, table);

        if (!player.isBot())
            client.sendDirectText("It's your turn", player.getId());
    }

    public void onReset(WireClient client) throws Exception {
        deleteTable(client);
        onPrint(client);
    }

    public void onPrint(WireClient client) throws Exception {
        Table table = getTable(client);
        client.sendText(table.printPlayers());
    }

    public void onMemberJoin(WireClient client, List<UUID> userIds) throws Exception {
        Collection<User> users = client.getUsers(userIds);
        for (User user : users) {
            addNewPlayer(client, user, false);
        }
        saveTable(client);
    }

    public void onMemberLeave(WireClient client, List<UUID> userIds) {
        Table table = getTable(client);
        for (UUID userId : userIds)
            table.removePlayer(userId);
        saveTable(client);
    }

    public void onBots(WireClient client, Action cmd) throws Exception {
        Table table = getTable(client);
        for (Player player : table.getActivePlayers()) {
            if (player.isBot()) {
                Betman betman = new Betman(client, table, player);
                boolean called = betman.action(cmd, this::check);
                if (called) {
                    onBots(client, cmd);
                    break;
                }
            }
        }
        check(client);
    }

    private void flopCards(WireClient client, Table table, int number) throws Exception {
        if (number == 0)
            return;

        table.newBet();

        for (int i = 0; i < number; i++)
            table.flopCard();

        for (Player player : table.getActivePlayers()) {
            if (!player.isBot()) {
                //executor.execute(() -> sendCards(client, table, player));
                sendCards(client, table, player);
            }
        }

        for (Player player : table.getFoldedPlayers()) {
            if (!player.isBot()) {
                //executor.execute(() -> sendBoard(client, table, player));
                sendBoard(client, table, player);
            }
        }

        onBots(client, Action.CALL);
    }

    private void dealPlayers(WireClient client, Table table) {
        Collection<Player> players = table.getPlayers();
        for (Player player : players) {
            table.blind(player);//take SB or BB

            Card a = table.dealCard(player);
            Card b = table.dealCard(player);

            if (!player.isBot()) {
                //executor.execute(() -> sendHoleCards(client, player, a, b));
                sendHoleCards(client, player, a, b);
            }
        }
    }

    private void sendHoleCards(WireClient client, Player player, Card a, Card b) {
        try {
            byte[] image = Images.getImage(a, b);
            client.sendDirectPicture(image, MIME_TYPE, player.getId());
        } catch (Exception e) {
            Logger.error("sendHoleCards: %s", e);
        }
    }

    private void sendBoard(WireClient client, Table table, Player player) {
        try {
            byte[] image = Images.getImage(table.getBoard());
            client.sendDirectPicture(image, MIME_TYPE, player.getId());
        } catch (Exception e) {
            Logger.error("sendBoard: %s", e);
        }
    }

    private void sendCards(WireClient client, Table table, Player player) {
        try {
            byte[] image = Images.getImage(player.getCards(), table.getBoard());
            client.sendDirectPicture(image, MIME_TYPE, player.getId());

            Probability prob = new Probability(table.getBoard(), player.getCards());
            Hand bestHand = player.getBestHand();
            float chance = prob.chance(player);

            String hand = String.format("You have **%s** (%.1f%%)", bestHand, chance);
            client.sendDirectText(hand, player.getId());
        } catch (Exception e) {
            Logger.error("sendCards: %s", e);
        }
    }

    private boolean check(WireClient client) {
        try {
            Table table = getTable(client);

            if (table.isSomeoneKaputt()) {
                flopCards(client, table, 5 - table.getBoard().size()); // flop remaining cards
                showdown(client, table);
                return true;
            }

            if (table.isAllFolded()) {
                showdown(client, table);
                return true;
            }

            if (table.isAllCalled() && table.isShowdown()) {
                showdown(client, table);
                return true;
            }

            if (table.isAllCalled()) {
                flopCards(client, table, table.isFlopped() ? 1 : 3); // turn or river or flop
                return true;
            }
        } catch (Exception e) {
            Logger.error("Poker.check: %s", e);
        } finally {
            saveTable(client);
        }

        return false;
    }

    private void showdown(WireClient client, Table table) {
        try {
            if (table.getPot() == 0)
                return;

            Player winner = table.getWinner();
            int pot = table.flushPot(winner);

            printWinner(client, table, winner, pot);

            for (Player player : table.collectPlayers()) {
                client.sendText(String.format("%s has ran out of chips and was kicked out", player.getName()));
            }

            printSummary(client, table);

            if (table.getPlayers().size() <= 1) {
                Logger.info("Updating ranking for: %s, money: %s", winner.getName(), table.getMoney());
                ranking.winner(winner.getId(), table.getMoney());
                saveRanking();

                client.ping();
                deleteTable(client);
            }
        } catch (Exception e) {
            Logger.error("showdown: %s", e);
        }
    }

    private Table getTable(WireClient client) {
        return getTable(client, true);
    }

    private Table getTable(WireClient client, boolean create) {
        return tables.computeIfAbsent(client.getId(), k -> {
            try {

                String jsonTable = db.getTable(client.getId());
                if (jsonTable != null) {
                    return deserializeTable(jsonTable);
                }
                return create ? newTable(client) : null;
            } catch (Exception e) {
                Logger.error(e.toString());
                return null;
            }
        });
    }

    private void deleteTable(WireClient client) {
        UUID botId = client.getId();
        tables.remove(botId);
        db.deleteTable(botId);
    }

    private void saveTable(WireClient client) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Table table = getTable(client, false);
            if (table != null) {
                String jsonTable = mapper.writeValueAsString(table);
                db.insertTable(client.getId(), jsonTable);
            }
        } catch (Exception e) {
            Logger.error(e.toString());
        }
    }

    private Table newTable(WireClient client) throws IOException, HttpException {
        Table table = new Table(new Deck());
        Conversation conversation = client.getConversation();
        for (Member member : conversation.members) {
            if (member.service == null) {
                User user = client.getUser(member.id);
                Player player = table.addPlayer(user, false);
                int fee = ranking.commit(player.getId(), player.getName());
                table.commitFee(fee);
            }
        }
        saveRanking();
        Logger.info("New Table with %d players", table.getPlayers().size());
        return table;
    }

    private Table deserializeTable(String jsonTable) throws java.io.IOException {
        ObjectMapper mapper = new ObjectMapper();
        Table table = mapper.readValue(jsonTable, Table.class);
        for (Player player : table.getPlayers()) {
            player.setBoard(table.getBoard());
            ranking.register(player.getId(), player.getName());
        }
        saveRanking();
        Logger.info("Loaded table with %d players from storage", table.getPlayers().size());
        return table;
    }

    private void addNewPlayer(WireClient client, User user, boolean bot) throws Exception {
        Table table = getTable(client);
        Player player = table.addPlayer(user, bot);
        String text = String.format("%s has joined the table with %d chips",
                player.getName(),
                player.getChips());
        client.sendText(text);
        int fee = ranking.commit(player.getId(), player.getName());
        table.commitFee(fee);
        saveRanking();
    }

    private void printWinner(WireClient client, Table table, Player winner, int pot) throws Exception {
        Collection<Player> activePlayers = table.getActivePlayers();
        if (activePlayers.size() <= 1) {
            String text = String.format("%s has won pot of %d chips", winner.getName(), pot);
            client.sendText(text);
            return;
        }

        for (Player player : activePlayers) {
            Hand bestHand = player.getBestHand();
            String name = player.equals(winner)
                    ? String.format("**%s** has won %d chips", player.getName(), pot)
                    : player.getName();
            String text = String.format("%s with %s", name, bestHand);
            client.sendText(text);

            // Show board cards with the ones not used in best hand as gray
            ArrayList<BufferedImage> board = new ArrayList<>();
            for (Card c : table.getBoard()) {
                Color color = bestHand.getCards().contains(c) ? Color.WHITE : Color.LIGHT_GRAY;
                BufferedImage image = Images.getBufferedImage(c, color);
                board.add(image);
            }

            // Show hole cards with the ones not used in best hand as gray
            ArrayList<BufferedImage> hole = new ArrayList<>();
            for (Card c : player.getCards()) {
                Color color = bestHand.getCards().contains(c) ? Color.WHITE : Color.LIGHT_GRAY;
                BufferedImage image = Images.getBufferedImage(c, color);
                hole.add(image);
            }

            BufferedImage attached = Images.attach(hole, board);
            client.sendPicture(Images.getBytes(attached), MIME_TYPE);
        }
    }

    private void printSummary(WireClient client, Table table) throws Exception {
        StringBuilder sb = new StringBuilder("```");
        for (Player player : table.getTopPlayers()) {
            String text = String.format("%-15s chips: %d",
                    player.getName(),
                    player.getChips());
            sb.append(text);
            sb.append("\n");
        }
        sb.append("```");
        client.sendText(sb.toString());
    }

    private Ranking loadRanking() {
        Ranking ret;
        try {
            String json = db.getRanking();
            ObjectMapper mapper = new ObjectMapper();
            ret = mapper.readValue(json, Ranking.class);
            Logger.info("Ranking size:", ranking.size());
        } catch (Exception e) {
            Logger.error("loadRanking: %s", e);
            ret = new Ranking();
        }
        return ret;
    }

    private void saveRanking() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(ranking);
            db.saveRanking(json);
        } catch (Exception e) {
            Logger.error("saveRanking: %s", e);
        }
    }

    public boolean onKickOut(WireClient client, String name) throws Exception {
        Table table = getTable(client);
        Player player = table.findPlayer(name);
        if (player != null) {
            if (table.removePlayer(player.getId())) {
                client.sendText(name + " was kicked out");
                return true;
            }
        }
        client.sendText("Could not kick out " + name);
        return false;
    }
}
