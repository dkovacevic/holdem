package com.wire.bots.holdem.game;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

public class Ranking {
    private static final int FEE = 5;
    @JsonProperty
    private final HashMap<UUID, Rank> rankings = new HashMap<>();

    String print() {
        ArrayList<Rank> list = new ArrayList<>(rankings.values());
        list.sort(Comparator.reverseOrder());
        StringBuilder sb = new StringBuilder("```\n");
        sb.append(String.format("%-20s\t%s\t%s\n", "Name", "Wins", "$"));
        list.forEach(v -> sb.append(String.format("%-20s\t%d\t\t%d\n", v.name, v.wins, v.money)));
        return sb.append("```").toString();
    }

    void winner(UUID id, int money) {
        Rank rank = rankings.get(id);
        rank.money += money;
        rank.wins++;
    }

    int commit(UUID id, String name) {
        Rank r = getRank(id, name);
        r.money -= FEE;
        return FEE;
    }

    void register(UUID id, String name) {
        getRank(id, name);
    }

    public int size() {
        return rankings.size();
    }

    private Rank getRank(UUID id, String name) {
        return rankings.computeIfAbsent(id, k -> {
            Rank rank = new Rank();
            rank.name = name;
            return rank;
        });
    }

    static class Rank implements Comparable<Rank> {
        @JsonProperty
        String name;
        @JsonProperty
        int money;
        @JsonProperty
        int wins;

        @Override
        public int compareTo(Rank other) {
            if (money > other.money) return 1;
            if (money < other.money) return -1;
            return 0;
        }
    }
}
