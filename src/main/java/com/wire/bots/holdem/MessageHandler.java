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

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            Table table = getTable(client);

            String cmd = msg.getText().toLowerCase().trim();
            if (cmd.startsWith("@raise")) {
                String trim = cmd.replace("@raise", "").trim();
                int raise = Integer.parseInt(trim);
                table.raise(msg.getUserId(), raise);
                return;
            }

            switch (cmd) {
                case "@deal": {
                    table.shuffle();

                    for (Player player : table.getPlayers()) {
                        table.call(player.getUserId()); //take small blind

                        table.dealCard(player);
                        table.dealCard(player);

                        // Send cards to this player
                        byte[] image = Images.getImage(player.getCards());
                        client.sendPicture(image, MIME_TYPE, player.getUserId());
                    }
                }
                break;
                case "@flop":
                    flopCard(client, table, 3);
                    break;
                case "@turn":
                    flopCard(client, table, 1);
                    break;
                case "@river":
                    flopCard(client, table, 1);
                    break;
                case "@fold":
                    table.fold(msg.getUserId());
                    break;
                case "@call":
                    table.call(msg.getUserId());
                    break;
                case "@check":
                    table.call(msg.getUserId());
                    break;
                case "@showdown":
                    showdown(client, table);
                    break;
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    private void flopCard(WireClient client, Table table, int number) throws Exception {
        table.resetBet();

        for (int i = 0; i < number; i++)
            table.flopCard();

        byte[] image = Images.getImage(table.getBoard());
        client.sendPicture(image, MIME_TYPE);
    }

    private void showdown(WireClient client, Table table) throws Exception {
        ArrayList<Card> board = table.getBoard();
        if (board.size() != 5) {
            client.sendText("It's not time for the showdown.", 5000);
            return;
        }

        Player winner = table.getWinner();
        int pot = table.flushPot(winner);

        for (Player player : table.getPlayers()) {
            if (player.isActive()) {
                Hand bestHand = player.getBestHand();

                String w = player.equals(winner) ? "**" : "";
                String p = player.equals(winner) ? ", pot: " + pot : "";
                String text = String.format("%s%s%s (%d) %s%s",
                        w,
                        player.getName(),
                        w,
                        player.getChips(),
                        bestHand,
                        p);
                client.sendText(text);

                byte[] image = Images.getImage(bestHand.getCards());
                client.sendPicture(image, MIME_TYPE);
            }
        }
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
