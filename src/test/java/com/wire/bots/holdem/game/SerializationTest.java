package com.wire.bots.holdem.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

public class SerializationTest {
    @Test
    public void serializeTable() throws IOException {
        Deck deck = new Deck();
        Table table = new Table(deck);
        for (int i = 0; i < 5; i++) {
            Player player = table.addPlayer("" + i, "" + i, false);
            table.dealCard(player);
            table.dealCard(player);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(player);
            Player player1 = mapper.readValue(json, Player.class);

            assert player.getRole() == player1.getRole();
            assert player.getCards().get(0).equals(player1.getCards().get(0));
            assert player.getCards().get(1).equals(player1.getCards().get(1));
        }

        Player bot = table.addPlayer("bot", "bot", true);
        table.dealCard(bot);
        table.dealCard(bot);

        table.flopCard();
        table.flopCard();
        table.flopCard();
        table.flopCard();
        table.flopCard();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(table);

        Table table2 = mapper.readValue(json, Table.class);
        for (Player player : table2.getPlayers())
            player.setBoard(table2.getBoard());

        assert table2.getPlayers().size() == table.getPlayers().size();
    }
}
