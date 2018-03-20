//
// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

package com.wire.bots.holdem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.factories.StorageFactory;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.Member;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.storage.Storage;
import com.wire.bots.sdk.tools.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MessageHandler extends MessageHandlerBase {
    private static final ConcurrentHashMap<String, Table> tables = new ConcurrentHashMap<>();
    // constance
    private static final String MIME_TYPE = "image/png";
    private static final String RANKING_FILENAME = "ranking";
    // Commands
    private static final String RAISE = "raise";
    private static final String R = "r";          // short for `raise`
    private static final String DEAL = "deal";    // deal cards
    private static final String F = "f";          // short for `fold`
    private static final String FOLD = "fold";
    private static final String CALL = "call";
    private static final String C = "c";          // short for `call`
    private static final String CHECK = "check";  // equivalent to `call`
    private static final String ADD_BOT = "add bot";
    private static final String RANKING = "ranking";
    private static final String TABLE_JSON = "table.json";
    private static final String BET = "bet";
    private static final String B = "b";
    private static final String RESET = "reset";
    private static final String PRINT = "print";
    private static Ranking ranking = new Ranking();
    private final StorageFactory storageFactory;
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(4);

    MessageHandler(StorageFactory storageFactory) {
        this.storageFactory = storageFactory;
        loadRanking();
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            Table table = getTable(client);

            Player player = table.getPlayer(msg.getUserId());

            Action action = parseCommand(msg.getText());
            switch (action) {
                case PRINT:
                    client.sendText(table.printPlayers());
                    break;
                case RESET:
                    table = closeTable(client);
                    client.sendText(String.format("New game. Players: %s blind %d - raise %d",
                            table.printPlayers(),
                            table.getSmallBlind(),
                            table.getRaise()));
                    break;
                case DEAL: {
                    Logger.info("New Deal with %d players", table.getPlayers().size());

                    Player turn = table.shiftRoles();
                    table.shuffle();
                    turn.setTurn(true);
                    table.turn();

                    client.sendText(String.format("%d. %s blind %d - raise %d",
                            table.getRoundNumber(),
                            table.printPlayers(),
                            table.getSmallBlind(),
                            table.getRaise()));

                    dealPlayers(client, table);
                }
                break;
                case RAISE:
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
                    break;
                case CALL:
                    table.call(player);

                    if (player.getCall() > 0) {
                        table.refund(player.getCall());
                    }
                    break;
                case FOLD:
                    table.fold(player);
                    break;
                case ADD_BOT: {
                    User user = new User();
                    user.id = UUID.randomUUID().toString();
                    user.name = Betman.randomName();
                    addNewPlayer(client, table, user, true);
                }
                break;
                case RANKING:
                    client.sendText(ranking.print());
            }

            if (!betmanCall(client, table, action))
                check(client, table);
        } catch (Exception e) {
            Logger.error("onText: %s", e);
        }
    }

    private void dealPlayers(WireClient client, Table table) throws Exception {
        Collection<Player> players = table.getPlayers();
        for (Player player : players) {
            table.blind(player);//take SB or BB

            Card a = table.dealCard(player);
            Card b = table.dealCard(player);

            if (!player.isBot()) {
                byte[] image = Images.getImage(a, b);
                client.sendDirectPicture(image, MIME_TYPE, player.getId());
            }
        }
    }

    private boolean betmanCall(WireClient client, Table table, Action cmd) {
        for (Player player : table.getActivePlayers()) {
            if (player.isBot()) {
                Betman betman = new Betman(client, table, player);
                boolean called = betman.action(cmd, this::check);
                if (called)
                    return betmanCall(client, table, cmd);
            }
        }
        return false;
    }

    private Boolean check(WireClient client, Table table) {
        try {
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
            saveState(table, client.getId());
        }
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

        betmanCall(client, table, Action.CALL);
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
                ranking.winner(winner.getId(), table.getMoney());
                saveRanking();

                client.ping();
                table = closeTable(client);
                saveState(table, client.getId());
            }
        } catch (Exception e) {
            Logger.error("showdown: %s", e);
        }
    }

    @Override
    public void onNewConversation(WireClient client) {
        try {
            Table table = getTable(client);
            for (Player player : table.getPlayers()) {
                String text = String.format("Player: %s has joined the table with %d chips",
                        player.getName(),
                        player.getChips());
                client.sendText(text);
                ranking.player(player.getId(), player.getName());
            }
            client.sendText("Add more participants. Type: `deal` to start. `call`, `raise`, `fold` when betting..." +
                    " If you feel lonely type: `add bot`");
            saveState(table, client.getId());
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    @Override
    public void onMemberJoin(WireClient client, ArrayList<String> userIds) {
        try {
            Table table = getTable(client);
            Collection<User> users = client.getUsers(userIds);
            for (User user : users) {
                addNewPlayer(client, table, user, false);
            }

            saveState(table, client.getId());
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    @Override
    public void onMemberLeave(WireClient client, ArrayList<String> userIds) {
        try {
            Table table = getTable(client);
            for (String userId : userIds)
                table.removePlayer(userId);

            saveState(table, client.getId());
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    private Table getTable(WireClient client) {
        return tables.computeIfAbsent(client.getId(), k -> {
            try {
                Storage storage = storageFactory.create(client.getId());
                String jsonTable = storage.readFile(TABLE_JSON);
                if (jsonTable != null) {
                    return deserializeTable(jsonTable);
                }
                return newTable(client);
            } catch (Exception e) {
                Logger.error(e.toString());
                return null;
            }
        });
    }

    private Table closeTable(WireClient client) throws Exception {
        String botId = client.getId();
        Table table = newTable(client); //todo delete json instead of creating new table, ffs!
        tables.put(botId, table);
        saveState(table, client.getId());
        return table;
    }

    private Table newTable(WireClient client) throws IOException {
        Table table = new Table(new Deck());
        Conversation conversation = client.getConversation();
        for (Member member : conversation.members) {
            if (member.service == null) {
                User user = client.getUser(member.id);
                Player player = table.addPlayer(user, false);
                ranking.player(player.getId(), player.getName());
            }
        }
        Logger.info("New Table with %d players", table.getPlayers().size());
        return table;
    }

    private Table deserializeTable(String jsonTable) throws java.io.IOException {
        ObjectMapper mapper = new ObjectMapper();
        Table table = mapper.readValue(jsonTable, Table.class);
        for (Player player : table.getPlayers()) {
            player.setBoard(table.getBoard());
        }
        Logger.info("Loaded table from storage");
        return table;
    }

    private void addNewPlayer(WireClient client, Table table, User user, boolean bot) throws Exception {
        Player player = table.addPlayer(user, bot);
        String text = String.format("%s has joined the table with %d chips",
                player.getName(),
                player.getChips());
        client.sendText(text);
        ranking.player(player.getId(), player.getName());
    }

    private Action parseCommand(String cmd) {
        switch (cmd.toLowerCase().trim()) {
            case PRINT:
                return Action.PRINT;
            case RESET:
                return Action.RESET;
            case DEAL:
                return Action.DEAL;
            case R:
            case RAISE:
            case BET:
            case B:
                return Action.RAISE;
            case C:
            case CHECK:
            case CALL:
                return Action.CALL;
            case FOLD:
            case F:
                return Action.FOLD;
            case ADD_BOT:
                return Action.ADD_BOT;
            case RANKING:
                return Action.RANKING;
            default:
                return Action.UNKNOWN;
        }
    }

    private void saveState(Table table, String botId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonTable = mapper.writeValueAsString(table);
            Storage storage = storageFactory.create(botId);
            storage.saveFile(TABLE_JSON, jsonTable);
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

    private void loadRanking() {
        try {
            Storage storage = storageFactory.create("");
            String json = storage.readGlobalFile(RANKING_FILENAME);
            ObjectMapper mapper = new ObjectMapper();
            ranking = mapper.readValue(json, Ranking.class);
        } catch (Exception e) {
            Logger.error("loadRanking: %s", e);
        }
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
