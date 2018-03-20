package com.wire.bots.holdem;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Ranking {
    @JsonProperty
    private final HashMap<String, Rank> rankings = new HashMap<>();

    public String print() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Rank> list = new ArrayList<>();
        list.addAll(rankings.values());
        list.sort(Comparator.reverseOrder());
        list.forEach(v ->
                sb.append(String.format("%-20s $ %d", v.name, v.money))
                        .append("\n"));
        return sb.toString();
    }

    public void winner(String id, int money) {
        Rank rank = rankings.get(id);
        rank.money += money;
    }

    public void player(String id, String name) {
        Rank r = rankings.computeIfAbsent(id, k -> {
            Rank rank = new Rank();
            rank.money = 0;
            rank.name = name;
            return rank;
        });
        r.money -= 5;
    }

    static class Rank implements Comparable<Rank> {
        @JsonProperty
        String name;
        @JsonProperty
        int money;

        @Override
        public int compareTo(Rank other) {
            if (money > other.money) return 1;
            if (money < other.money) return -1;
            return 0;
        }
    }
}
