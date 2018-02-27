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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class MessageHandler extends MessageHandlerBase {
    private static final String URL = "https://raw.githubusercontent.com/hayeah/playing-cards-assets/master/png";
    private static final int SHIFT = 38;
    private static final int WIDTH = 222;
    private static final int HEIGHT = 323;

    private Round round = new Round();
    private Deck deck = new Deck();

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            if (msg.getText().startsWith("@hand")) {
                round = new Round();
                deck = new Deck();

                Conversation conversation = client.getConversation();

                //client.sendText("Your hand:");
                for (Member member : conversation.members) {
                    if (member.service != null)
                        continue;

                    Card card1 = deck.drawFromDeck();
                    Card card2 = deck.drawFromDeck();

                    Player player = new Player(member.id, round.getBoard());
                    player.addCard(card1);
                    player.addCard(card2);

                    round.addPlayer(player);

                    byte[] image = getImage(player.getCards());
                    client.sendPicture(image, "image/png", member.id);
                }

                round.addCardToBoard(deck.drawFromDeck());
                round.addCardToBoard(deck.drawFromDeck());
                round.addCardToBoard(deck.drawFromDeck());

                return;
            }

            if (msg.getText().startsWith("@board")) {
                byte[] image = getImage(round.getBoard());
                client.sendPicture(image, "image/png");
                return;
            }

            if (msg.getText().startsWith("@new")) {
                ArrayList<Card> board = round.getBoard();
                if (board.size() >= 5) {
                    client.sendText("There are 5 cards on the board already");
                    return;
                }

                round.addCardToBoard(deck.drawFromDeck());

                byte[] image = getImage(board);
                client.sendPicture(image, "image/png");
                return;
            }

            if (msg.getText().startsWith("@fold")) {
                round.foldPlayer(msg.getUserId());
            }

            if (msg.getText().startsWith("@drop")) {
                Player winner = round.getWinner();

                for (Player player : round.getPlayers()) {
                    User user = client.getUser(player.getUserId());

                    String w = player.getUserId().equals(winner.getUserId()) ? "**" : "";
                    String text = String.format("%s%s%s with: %s", w, user.name, w, player.bestHand().display());
                    client.sendText(text.trim());

                    byte[] image = getImage(player.bestHand().getCards());
                    client.sendPicture(image, "image/png");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getImage(Collection<Card> collection) throws IOException {
        Card[] cards = new Card[collection.size()];
        collection.toArray(cards);
        ArrayList<BufferedImage> load = load(cards);
        BufferedImage combine = combine(load);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(combine, "png", output);
        return output.toByteArray();
    }

    private static BufferedImage combine(ArrayList<BufferedImage> images) {
        int size = images.size() - 1;
        final int width = WIDTH + size * SHIFT;

        BufferedImage result = new BufferedImage(width, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        for (int i = 0; i < size; i++) {
            BufferedImage image = images.get(i);

            BufferedImage sub = image.getSubimage(0, 0, SHIFT, HEIGHT);
            g.drawImage(sub, i * SHIFT, 0, null);
        }
        BufferedImage image = images.get(size);
        g.drawImage(image, size * SHIFT, 0, null);
        return result;
    }

    private static BufferedImage getBufferedImage(Card card1) throws IOException {
        try (InputStream input = new URL(String.format("%s/%s.png", URL, card1)).openStream()) {
            return ImageIO.read(input);
        }
    }

    private static ArrayList<BufferedImage> load(Card... cards) throws IOException {
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (Card card : cards)
            images.add(getBufferedImage(card));
        return images;
    }
}
