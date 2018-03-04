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

import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.Member;
import com.wire.bots.sdk.server.model.User;
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

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            Table table = getTable(client);

            String cmd = msg.getText().toLowerCase().trim();
            String userId = msg.getUserId();

            switch (cmd) {
                case DEAL: {
                    table.shiftRoles();

                    table.shuffle();

                    client.sendText(String.format("%s- round %d - small blind %d - raise %d",
                            table.printPlayers(),
                            table.getRoundNumber(),
                            table.getSmallBlind(),
                            table.getRaise()));

                    for (Player player : table.getPlayers()) {
                        table.blind(player.getId()); //take SB or BB

                        Card a = table.dealCard(player);
                        Card b = table.dealCard(player);

                        if (!player.isBot()) {
                            byte[] image = Images.getImage(a, b);
                            client.sendPicture(image, MIME_TYPE, player.getId());
                        }
                    }

                    betmanCall(client, table);
                }
                break;
                case R:
                case RAISE: {
                    int newBet = table.raise(userId);
                    if (newBet != -1) {
                        Player player = table.getPlayer(userId);
                        client.sendText(String.format("%s raised by %d to %d",
                                player.getName(),
                                table.getRaise(),
                                newBet));

                        betmanCall(client, table);
                        check(client, table);
                    }
                }
                break;
                case C:
                case CHECK:
                case CALL: {
                    int call = table.call(userId);
                    if (call != -1) {
                        Player player = table.getPlayer(userId);
                        client.sendDirectText(String.format("You called with %d chips",
                                call), player.getId());
                        betmanCall(client, table);
                        check(client, table);
                    }

                }
                break;
                case FOLD:
                case F:
                    if (table.fold(userId)) {
                        betmanCall(client, table);
                        check(client, table);
                    }
                    break;
                case ADD_BOT: {
                    User user = new User();
                    user.id = BETMAN;
                    user.name = BETMAN;
                    newPlayer(client, table, user, true);
                }
                break;
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    private void betmanCall(WireClient client, Table table) throws Exception {
        int betman = table.call(BETMAN);
        if (betman != -1) {
            Player player = table.getPlayer(BETMAN);
            client.sendText(String.format("%s called with %d chips",
                    player.getName(),
                    betman));

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
                flopCard(client, table, table.isFlopped() ? 1 : 3); // turn or river or flop
        }
    }

    private void flopCard(WireClient client, Table table, int number) throws Exception {
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
    }

    private void showdown(WireClient client, Table table) throws Exception {
        Player winner = table.getWinner();
        int pot = table.flushPot(winner);

        Collection<Player> activePlayers = table.getActivePlayers();
        if (activePlayers.size() > 1) {
            for (Player player : activePlayers) {
                String name = player.equals(winner)
                        ? String.format("**%s** has won %d chips", player.getName(), pot)
                        : player.getName();
                String text = String.format("%s with %s",
                        name,
                        player.getBestHand());
                client.sendText(text);

                byte[] image = Images.getImage(player.getBestHand().getCards());
                client.sendPicture(image, MIME_TYPE);
            }
        } else {
            String text = String.format("%s has won pot of %d chips", winner.getName(), pot);
            client.sendText(text);
        }

        for (Player player : table.collectPlayers()) {
            client.sendText(String.format("%s has ran out of chips and was kicked out", player.getName()));
        }

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
                newPlayer(client, table, user, false);
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    @Override
    public void onMemberLeave(WireClient client, ArrayList<String> userIds) {
        Table table = getTable(client);
        for (String userId : userIds)
            table.removePlayer(userId);
    }

    private Table getTable(WireClient client) {
        return tables.computeIfAbsent(client.getId(), k -> {
            Table table = new Table(new Deck());
            try {
                Conversation conversation = client.getConversation();
                for (Member member : conversation.members) {
                    if (member.service == null) {
                        User user = client.getUser(member.id);
                        table.addPlayer(user, false);
                    }
                }
            } catch (IOException e) {
                Logger.error(e.toString());
            }
            return table;
        });
    }

    private void newPlayer(WireClient client, Table table, User user, boolean bot) throws Exception {
        Player player = table.addPlayer(user, bot);
        String text = String.format("%s has joined the table with %d chips",
                player.getName(),
                player.getChips());
        client.sendText(text);
    }

    private void closeTable(WireClient client) {
        tables.remove(client.getId());
    }
}
