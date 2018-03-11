package com.wire.bots.holdem;

import com.wire.bots.holdem.strategies.*;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.tools.Logger;

import java.util.Random;
import java.util.Timer;
import java.util.function.BiFunction;

class Betman {
    private static final int DELAY = 2000;

    private final Timer timer = new Timer("Betman");
    private final WireClient client;
    private final Table table;
    private Player bot;

    Betman(WireClient client, Table table, Player bot) {
        this.client = client;
        this.table = table;
        this.bot = bot;
    }

    static String randomName() {
        String[] names = new String[]{"Betman", "Superman", "Cagil", "Irmak", "Richie", "Steve", "Dave", "Lay Z", "CEO"};
        Random rnd = new Random();
        return names[rnd.nextInt(names.length)];
    }

    private Action action(Action cmd) {
        return bot.isTurn() ? chooseStrategy().action(cmd) : Action.DEAL;
    }

    private Strategy chooseStrategy() {
        if (bot.getBoard().size() == 3)
            return new LoosePassive(bot);
        if (bot.getChips() < 50)
            return new TightPassive(bot);
        if (bot.getCall() > 20)
            return new TightAggressive(bot);

        return new LooseAggressive(bot);
    }

    boolean action(Action cmd, BiFunction<WireClient, Table, Boolean> check) {
        boolean ret = false;
        try {
            if (table.getPot() != 0 && bot.isTurn()) {
                switch (action(cmd)) {
                    case CALL:
                        ret = call();
                        break;
                    case RAISE:
                        ret = raise();
                        break;
                    case FOLD:
                        ret = fold();
                        break;
                    default:
                        return false;
                }
            }
        } catch (Exception e) {
            Logger.error(e.toString());
        } finally {
            check.apply(client, table);
        }

        return ret;
    }

    private boolean fold() throws Exception {
        if (table.fold(bot)) {
            client.sendText(String.format("%s has folded", bot.getName()));
            return true;
        }
        return false;
    }

    private boolean raise() throws Exception {
        int raise = table.raise(bot);
        if (raise != -1) {
            client.sendText(String.format("%s(%d) raised by %d, pot %d",
                    bot.getName(),
                    bot.getChips(),
                    raise,
                    table.getPot()));
            return true;
        }
        return false;
    }

    private boolean call() throws Exception {
        int call = table.call(bot);
        if (call != -1) {
            String msg = call == 0 ? String.format("%s(%d) checked. pot: %d",
                    bot.getName(),
                    bot.getChips(),
                    table.getPot())
                    : String.format("%s(%d) called with %d chips. pot: %d",
                    bot.getName(),
                    bot.getChips(),
                    call,
                    table.getPot());

            client.sendText(msg);

            int dept = bot.getCall();
            if (dept > 0) {
                table.refund(dept);
            }
            return true;
        }
        return false;
    }
}
