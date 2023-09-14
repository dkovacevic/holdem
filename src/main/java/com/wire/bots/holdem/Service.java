package com.wire.bots.holdem;

import com.wire.lithium.Server;
import com.wire.xenon.MessageHandlerBase;
import io.dropwizard.setup.Environment;

public class Service extends Server<Config> {
    public static Service instance;

    public static void main(String[] args) throws Exception {
        instance = new Service();
        instance.run(args);
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {

        return new MessageHandler();
    }

}
