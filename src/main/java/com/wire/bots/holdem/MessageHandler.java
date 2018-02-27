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

import java.util.ArrayList;

public class MessageHandler extends MessageHandlerBase {
    private static final String MIME_TYPE = "image/png";

    private Round round = new Round();
    private Deck deck = new Deck();

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            if (msg.getText().startsWith("@hand")) {
                round = new Round();
                deck = new Deck();

                Conversation conversation = client.getConversation();

                for (Member member : conversation.members) {
                    if (member.service != null)
                        continue;

                    Player player = round.addPlayer(member.id);

                    for (int i = 0; i < 2; i++)
                        player.addCard(deck.drawFromDeck());

                    // Send cards to this player
                    byte[] image = Images.getImage(player.getCards());
                    client.sendPicture(image, MIME_TYPE, player.getUserId());
                }

                round.addCardToBoard(deck.drawFromDeck());
                round.addCardToBoard(deck.drawFromDeck());
                round.addCardToBoard(deck.drawFromDeck());

                return;
            }

            if (msg.getText().startsWith("@board")) {
                byte[] image = Images.getImage(round.getBoard());
                client.sendPicture(image, MIME_TYPE);
                return;
            }

            if (msg.getText().startsWith("@new")) {
                ArrayList<Card> board = round.getBoard();
                if (board.size() >= 5) {
                    client.sendText("There are 5 cards on the board already");
                    return;
                }

                round.addCardToBoard(deck.drawFromDeck());

                byte[] image = Images.getImage(board);
                client.sendPicture(image, MIME_TYPE);
                return;
            }

            if (msg.getText().startsWith("@fold")) {
                round.foldPlayer(msg.getUserId());
            }

            if (msg.getText().startsWith("@drop")) {
                Player winner = round.getWinner();

                for (Player player : round.getPlayers()) {
                    User user = client.getUser(player.getUserId());
                    Hand bestHand = player.bestHand();

                    String w = player.equals(winner) ? "**" : "";
                    String text = String.format("%s%s%s with: %s", w, user.name, w, bestHand.display());
                    client.sendText(text);

                    byte[] image = Images.getImage(bestHand.getCards());
                    client.sendPicture(image, MIME_TYPE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
