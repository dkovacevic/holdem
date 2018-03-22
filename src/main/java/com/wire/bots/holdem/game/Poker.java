package com.wire.bots.holdem.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.Images;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.factories.StorageFactory;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.Member;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.storage.Storage;
import com.wire.bots.sdk.tools.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Poker {
    private static final ConcurrentHashMap<String, Table> tables = new ConcurrentHashMap<>();
    private static final String MIME_TYPE = "image/png";
    private static final String RANKING_FILENAME = "global_ranking";
    private static final String TABLE_JSON = "table.json";
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(4);
    private final Ranking ranking;
    private final StorageFactory storageFactory;

    public Poker(StorageFactory storageFactory) {
        this.storageFactory = storageFactory;
        this.ranking = loadRanking();
    }

    public void onRanking(WireClient client) throws Exception {
        client.sendText(ranking.print());
    }

    public void onAddBot(WireClient client) throws Exception {
        User user = new User();
        String name = Betman.randomName();
        user.id = name;
        user.name = name;
        addNewPlayer(client, user, true);
    }

    public void onFold(WireClient client, String userId) {
        Table table = getTable(client);
        Player player = table.getPlayer(userId);
        table.fold(player);
    }

    public void onCall(WireClient client, String userId) {
        Table table = getTable(client);
        Player player = table.getPlayer(userId);
        table.call(player);

        if (player.getCall() > 0) {
            table.refund(player.getCall());
        }
    }

    public void onRaise(WireClient client, String userId) throws Exception {
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
        closeTable(client);
        onPrint(client);
    }

    public void onPrint(WireClient client) throws Exception {
        Table table = getTable(client);
        client.sendText(table.printPlayers());
    }

    public void onMemberJoin(WireClient client, ArrayList<String> userIds) throws Exception {
        Collection<User> users = client.getUsers(userIds);
        for (User user : users) {
            addNewPlayer(client, user, false);
        }
        saveState(client);
    }

    public void onMemberLeave(WireClient client, ArrayList<String> userIds) {
        Table table = getTable(client);
        for (String userId : userIds)
            table.removePlayer(userId);
        saveState(client);
    }

    public void onBots(WireClient client, Action cmd) {
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

    private void flopCards(WireClient client, Table table, int number) {
        if (number == 0)
            return;

        table.newBet();

        for (int i = 0; i < number; i++)
            table.flopCard();

        for (Player player : table.getActivePlayers()) {
            if (!player.isBot()) {
                executor.execute(() -> sendCards(client, table, player));
            }
        }

        for (Player player : table.getFoldedPlayers()) {
            if (!player.isBot()) {
                executor.execute(() -> sendBoard(client, table, player));
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
                executor.execute(() -> sendHoleCards(client, player, a, b));
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

            return false;
        } finally {
            saveState(client);
        }
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
                closeTable(client);
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
                Storage storage = storageFactory.create(client.getId());
                String jsonTable = storage.readFile(TABLE_JSON);
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

    private void closeTable(WireClient client) throws Exception {
        String botId = client.getId();
        tables.remove(botId);
        Storage storage = storageFactory.create(botId);
        boolean deleteFile = storage.deleteFile(TABLE_JSON);
    }

    private Table newTable(WireClient client) throws IOException {
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

    private void saveState(WireClient client) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Table table = getTable(client, false);
            if (table != null) {
                String jsonTable = mapper.writeValueAsString(table);
                Storage storage = storageFactory.create(client.getId());
                storage.saveFile(TABLE_JSON, jsonTable);
            }
        } catch (Exception e) {
            Logger.error(e.toString());
        }
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
            String text = String.format("%s with %s",
                    name,
                    bestHand);
            client.sendText(text);

            byte[] image = Images.getImage(player.getCards(), bestHand.getCards());
            client.sendPicture(image, MIME_TYPE);
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
        Ranking ret = null;
        try {
            Storage storage = storageFactory.create("");
            String json = storage.readGlobalFile(RANKING_FILENAME);
            if (json != null) {
                ObjectMapper mapper = new ObjectMapper();
                ret = mapper.readValue(json, Ranking.class);
                Logger.info("Ranking size:", ranking.size());
            } else
                ret = new Ranking();
        } catch (Exception e) {
            Logger.error("loadRanking: %s", e);
        }
        return ret;
    }

    private void saveRanking() {
        try {
            Storage storage = storageFactory.create("");
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(ranking);
            storage.saveGlobalFile(RANKING_FILENAME, json);
        } catch (Exception e) {
            Logger.error("saveRanking: %s", e);
        }
    }
}
