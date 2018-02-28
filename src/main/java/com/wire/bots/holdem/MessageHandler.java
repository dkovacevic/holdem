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
    private static final String BET = "bet";    // equivalent to `raise`
    private static final String DEAL = "deal";
    private static final String FLOP = "flop";
    private static final String FOLD = "fold";
    private static final String CALL = "call";
    private static final String CHECK = "check"; // equivalent to `call`

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            Table table = getTable(client);

            String cmd = msg.getText().toLowerCase().replace("@", "").trim();
            String userId = msg.getUserId();

            if (cmd.startsWith(RAISE)) {
                String trim = cmd.replace(RAISE, "").trim();
                int raise = Integer.parseInt(trim);
                table.raise(userId, raise);
                return;
            }

            if (cmd.startsWith(BET)) {
                String trim = cmd.replace(BET, "").trim();
                int raise = Integer.parseInt(trim);
                table.raise(userId, raise);
                return;
            }

            switch (cmd) {
                case DEAL: {
                    table.shuffle();

                    for (Player player : table.getPlayers()) {
                        table.blind(player.getId()); //take small blind

                        table.dealCard(player);
                        table.dealCard(player);

                        // Send cards to this player
                        byte[] image = Images.getImage(player.getCards());
                        client.sendPicture(image, MIME_TYPE, player.getId());
                    }
                }
                break;
                case FLOP:
                    flopCard(client, table, 3);
                    break;
                case CALL:
                case CHECK:
                    table.call(userId);
                    if (table.isFlopped() && table.isAllCalled()) {
                        if (table.isShowdown())
                            showdown(client, table);
                        else
                            flopCard(client, table, 1); // turn or river
                    } else if (table.isAllPlaying()) {
                        // perform the Flop if all participants called
                        flopCard(client, table, 3);
                    }
                    break;
                case FOLD:
                    table.fold(userId);

                    if (table.isFlopped() && table.isAllCalled()) {
                        if (table.isShowdown())
                            showdown(client, table);
                        else
                            flopCard(client, table, 1); // turn or river
                    } else if (table.isAllPlaying()) {
                        // perform the Flop if all participants called
                        flopCard(client, table, 3);
                    } else if (table.isDone()) {
                        // if we have only one player left then the round is finished
                        showdown(client, table);
                    }
                    break;
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    private void flopCard(WireClient client, Table table, int number) throws Exception {
        table.newBet();

        for (int i = 0; i < number; i++)
            table.flopCard();

        /*
        byte[] image = Images.getImage(table.getBoard());
        client.sendPicture(image, MIME_TYPE);
        */

        for (Player player : table.getActivePlayers()) {
            // Send cards to this player
            byte[] image = Images.getImage(player.getCards(), table.getBoard());
            client.sendPicture(image, MIME_TYPE, player.getId());
        }
    }

    private void showdown(WireClient client, Table table) throws Exception {
        Player winner = table.getWinner();
        int pot = table.flushPot(winner);

        Collection<Player> activePlayers = table.getActivePlayers();
        if (activePlayers.size() > 1) {
            for (Player player : activePlayers) {
                String p = player.equals(winner) ? "pot: " + pot : "";
                String name = player.equals(winner) ? "Winner **" + player.getName() + "**" : player.getName();
                String text = String.format("%s: *%s* %s",
                        name,
                        player.getBestHand(),
                        p);
                client.sendText(text);

                byte[] image = Images.getImage(player.getBestHand().getCards());
                client.sendPicture(image, MIME_TYPE);
            }
        } else {
            String text = String.format("%s won pot of %d chips", winner.getName(), pot);
            client.sendText(text);
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
    }

    @Override
    public void onNewConversation(WireClient client) {
        try {
            client.sendText("Type: @deal");
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
                table.addPlayer(user);
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
                        table.addPlayer(user);
                    }
                }
            } catch (IOException e) {
                Logger.error(e.toString());
            }
            return table;
        });
    }
}
