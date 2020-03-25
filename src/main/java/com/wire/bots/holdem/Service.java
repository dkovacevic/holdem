package com.wire.bots.holdem;

import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.setup.Environment;

public class Service extends Server<Config> {
    private static Service instance;

    public static void main(String[] args) throws Exception {
        instance = new Service();
        instance.run(args);
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {

        return new MessageHandler();
    }

    @Override
    protected void migrateDBifNeeded(Configuration.Database database) {
    }

    @Override
    protected void buildJdbi(Configuration.Database database) {
    }

    public static Config CONFIG() {
        return instance.getConfig();
    }

    public static ClientRepo REPO() {
        return instance.getRepo();
    }
}
