package com.wire.bots.holdem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

public class RankingTest {
    @Test
    public void serializationTest() throws IOException {
        Ranking ranking = new Ranking();
        ranking.commit("1", "1");
        ranking.commit("2", "2");
        ranking.commit("3", "3");
        ranking.commit("4", "4");

        ranking.winner("3", 20);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(ranking);
        Ranking ranking1 = objectMapper.readValue(json, Ranking.class);
        System.out.println(ranking1.print());
    }
}
