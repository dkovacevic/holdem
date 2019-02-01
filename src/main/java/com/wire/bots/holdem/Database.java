package com.wire.bots.holdem;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class Database {
    private static final int TIMEOUT = 5000;
    private static final String TABLE = "table";
    private static final String GLOBAL = "global";
    private static final String RANKING = "ranking";
    private static JedisPool pool;
    private final String host;
    private final Integer port;
    private final String password;

    public Database(String host, Integer port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    private static JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(16);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    private static JedisPool pool(String host, Integer port, String password) {
        if (pool == null) {
            JedisPoolConfig poolConfig = buildPoolConfig();
            pool = new JedisPool(poolConfig, host, port, TIMEOUT, password);
        }
        return pool;
    }


    public String getTable(String id) {
        try (Jedis jedis = getConnection()) {
            String key = key(TABLE, id);
            return jedis.get(key);
        }
    }

    public void insertTable(String id, String table) {
        try (Jedis jedis = getConnection()) {
            String key = key(TABLE, id);
            jedis.set(key, table);
        }
    }

    public void deleteTable(String id) {
        try (Jedis jedis = getConnection()) {
            String key = key(TABLE, id);
            jedis.del(key);
        }
    }

    public String getRanking() {
        try (Jedis jedis = getConnection()) {
            String key = key(GLOBAL, RANKING);
            return jedis.get(key);
        }
    }

    public void saveRanking(String json) {
        try (Jedis jedis = getConnection()) {
            String key = key(GLOBAL, RANKING);
            jedis.set(key, json);
        }
    }

    private String key(String name, String id) {
        return String.format("holdem_%s_%s", name, id);
    }

    private Jedis getConnection() {
        return pool(host, port, password).getResource();
    }
}
