package com.wire.bots.holdem.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

public class RankingTest {
    @Test
    public void serializationTest() throws IOException {
        final UUID id3 = UUID.randomUUID();

        Ranking ranking = new Ranking();
        ranking.commit(UUID.randomUUID(), "1");
        ranking.commit(UUID.randomUUID(), "2");
        ranking.commit(id3, "3");
        ranking.commit(UUID.randomUUID(), "4");

        ranking.winner(id3, 20);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(ranking);
        Ranking ranking1 = objectMapper.readValue(json, Ranking.class);
        System.out.println(ranking1.print());
    }
}
