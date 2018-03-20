package com.wire.bots.holdem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

public class RankingTest {
    @Test
    public void serializationTest() throws IOException {
        Ranking ranking = new Ranking();
        ranking.player("1", "1");
        ranking.player("2", "2");
        ranking.player("3", "3");
        ranking.player("4", "4");

        ranking.winner("3", 20);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(ranking);
        Ranking ranking1 = objectMapper.readValue(json, Ranking.class);
        System.out.println(ranking1.print());
    }
}
