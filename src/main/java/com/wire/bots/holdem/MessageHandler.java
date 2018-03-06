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
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler extends MessageHandlerBase {
    private static final ConcurrentHashMap<String, Table> tables = new ConcurrentHashMap<>();
    private static final String MIME_TYPE = "image/png";
    // Commands
    private static final String RAISE = "raise";
    private static final String R = "r";          // short for `raise`
    private static final String DEAL = "deal";    // deal cards
    private static final String F = "f";          // short for `fold`
    private static final String FOLD = "fold";
    private static final String CALL = "call";
    private static final String C = "c";          // short for `call`
    private static final String CHECK = "check";  // equivalent to `call`
    private static final String ADD_BOT = "add bot";  // equivalent to `call`
    private static final String BETMAN = "Betman";
    private static final String TABLE_JSON = "table.json";
    private static final String BET = "bet";
    private static final String B = "b";
    private final StorageFactory storageFactory;

    MessageHandler(StorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            Table table = getTable(client);

            Player player = table.getPlayer(msg.getUserId());
            if (player == null)
                return;

            Action action = parseCommand(msg.getText());
            switch (action) {
                case DEAL: {
                    table.shiftRoles();

                    table.shuffle();

                    client.sendText(String.format("%d. %s blind %d - raise %d",
                            table.getRoundNumber(),
                            table.printPlayers(),
                            table.getSmallBlind(),
                            table.getRaise()));

                    dealPlayers(client, table);

                    betmanCall(client, table, action);
                }
                break;
                case RAISE:
                    int raise = table.raise(player);
                    if (raise != -1) {
                        client.sendText(String.format("%s raised by %d, pot %d",
                                player.getName(),
                                raise,
                                table.getPot()));

                        betmanCall(client, table, action);
                        check(client, table);
                    } else if (!player.isCalled()) {
                        client.sendDirectText("Raise failed due to insufficient funds", player.getId());
                    }
                    break;
                case CALL:
                    int call = table.call(player);
                    if (call != -1) {
                        if (player.getCall() > 0) {
                            table.refund(player.getCall());
                            showdown(client, table);
                        } else {
                            betmanCall(client, table, action);
                            check(client, table);
                        }
                    }
                    break;
                case FOLD:
                    if (table.fold(player)) {
                        betmanCall(client, table, action);
                        check(client, table);
                    }
                    break;
                case ADD_BOT:
                    if (table.getPlayer(BETMAN) == null) {
                        User user = new User();
                        user.id = BETMAN;
                        user.name = BETMAN;
                        addNewPlayer(client, table, user, true);
                    } else
                        client.sendDirectText("Only one bot player allowed", player.getId());
                    break;
            }
            saveState(table, client.getId());
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    private void dealPlayers(WireClient client, Table table) throws Exception {
        boolean showdown = false;
        Collection<Player> players = table.getPlayers();
        for (Player player : players) {
            boolean blind = table.blind(player);//take SB or BB

            if (!blind)
                showdown = true;

            Card a = table.dealCard(player);
            Card b = table.dealCard(player);

            if (!player.isBot()) {
                byte[] image = Images.getImage(a, b);
                client.sendPicture(image, MIME_TYPE, player.getId());
            }

            if (players.size() == 2 && player.getRole() == Role.SB)
                player.setRole(Role.Caller);
        }

        if (showdown)
            showdown(client, table);
    }

    private void betmanCall(WireClient client, Table table, Action cmd) throws Exception {
        Player betman = table.getPlayer(BETMAN);
        if (betman == null)
            return;

        Action action = betman.action(cmd);
        switch (action) {
            case CALL:
                int call = table.call(betman);
                if (call != -1) {
                    String msg = call == 0
                            ? String.format("%s checked", betman.getName())
                            : String.format("%s called with %d chips", betman.getName(), call);
                    client.sendText(msg);
                }
                break;
            case RAISE:
                if (table.raise(betman) != -1) {
                    client.sendText(String.format("%s raised by %d, pot %d",
                            betman.getName(),
                            table.getRaise(),
                            table.getPot()));
                }
                break;
            case FOLD:
                if (table.fold(betman)) {
                    client.sendText(String.format("%s folded like a bitch", betman.getName()));
                }
                break;
        }
    }

    private void check(WireClient client, Table table) throws Exception {
        if (table.isAllFolded()) {
            showdown(client, table);
            return;
        }

        if (table.isAllCalled()) {
            if (table.isShowdown())
                showdown(client, table);
            else
                flopCards(client, table, table.isFlopped() ? 1 : 3); // turn or river or flop
        }
    }

    private void flopCards(WireClient client, Table table, int number) throws Exception {
        table.newBet();

        for (int i = 0; i < number; i++)
            table.flopCard();

        client.sendText(String.format("Pot has %d chips", table.getPot()));

        for (Player player : table.getActivePlayers()) {
            if (!player.isBot()) {
                byte[] image = Images.getImage(player.getCards(), table.getBoard());
                client.sendPicture(image, MIME_TYPE, player.getId());
            }
        }

        for (Player player : table.getFoldedPlayers()) {
            if (!player.isBot()) {
                byte[] image = Images.getImage(table.getBoard());
                client.sendPicture(image, MIME_TYPE, player.getId());
            }
        }

        betmanCall(client, table, Action.DEAL);
    }

    private void showdown(WireClient client, Table table) throws Exception {
        Player winner = table.getWinner();
        int pot = table.flushPot(winner);

        printWinner(client, table, winner, pot);

        for (Player player : table.collectPlayers()) {
            client.sendText(String.format("%s has ran out of chips and was kicked out", player.getName()));
        }

        printSummary(client, table);

        if (table.getPlayers().size() <= 1) {
            client.ping();
            closeTable(client);
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
            }
            client.sendText("Type: `deal` to start. `call`, `raise`, `fold` when betting... Have fun!");
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
                    Table table = deserializeTable(jsonTable);
                    return table != null ? table : newTable(client);
                } else {
                    return newTable(client);
                }
            } catch (Exception e) {
                Logger.error(e.toString());
                return null;
            }
        });
    }

    private void closeTable(WireClient client) throws Exception {
        String botId = client.getId();
        Storage storage = storageFactory.create(botId);
        storage.deleteFile(TABLE_JSON);// todo check for false
        tables.remove(botId);
    }

    private Table newTable(WireClient client) throws IOException {
        Table table = new Table(new Deck());
        Conversation conversation = client.getConversation();
        for (Member member : conversation.members) {
            if (member.service == null) {
                User user = client.getUser(member.id);
                table.addPlayer(user, false);
            }
        }

        Logger.info("New table with %d players", table.getPlayers().size());

        return table;
    }

    private Table deserializeTable(String jsonTable) throws java.io.IOException {
        ObjectMapper mapper = new ObjectMapper();
        Table table = mapper.readValue(jsonTable, Table.class);
        for (Player player : table.getPlayers()) {
            player.setBoard(table.getBoard());
        }

        //todo remove this shit
        if (table.getPlayers().size() == 1)
            return null;

        Logger.info("Loaded table from storage");

        return table;
    }

    private void addNewPlayer(WireClient client, Table table, User user, boolean bot) throws Exception {
        Player player = table.addPlayer(user, bot);
        String text = String.format("%s has joined the table with %d chips",
                player.getName(),
                player.getChips());
        client.sendText(text);
    }

    private Action parseCommand(String cmd) {
        switch (cmd.toLowerCase().trim()) {
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
            default:
                return Action.UNKNOWN;
        }
    }

    private void saveState(Table table, String botId) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonTable = mapper.writeValueAsString(table);
        Storage storage = storageFactory.create(botId);
        storage.saveFile(TABLE_JSON, jsonTable);
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
            if (bestHand == null) {
                Logger.warning("Best hand == null, wtf?");
                continue;
            }

            String name = player.equals(winner)
                    ? String.format("**%s** has won %d chips", player.getName(), pot)
                    : player.getName();
            String text = String.format("%s with %s",
                    name,
                    bestHand);
            client.sendText(text);

            byte[] image = Images.getImage(bestHand.getCards());
            client.sendPicture(image, MIME_TYPE);
        }
    }

    private void printSummary(WireClient client, Table table) throws Exception {
        StringBuilder sb = new StringBuilder("```");
        for (Player player : table.getPlayers()) {
            String text = String.format("%-15s chips: %d",
                    player.getName(),
                    player.getChips());
            sb.append(text);
            sb.append("\n");
        }
        sb.append("```");
        client.sendText(sb.toString());
    }
}
