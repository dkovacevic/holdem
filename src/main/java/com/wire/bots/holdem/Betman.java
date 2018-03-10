package com.wire.bots.holdem;

import com.wire.bots.holdem.strategies.*;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.tools.Logger;

import java.util.Timer;
import java.util.TimerTask;
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

    Action action(Action cmd, BiFunction<WireClient, Table, Boolean> check) {
        Action action = action(cmd);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (table.getPot() != 0) {
                        switch (action) {
                            case CALL:
                                call();
                                break;
                            case RAISE:
                                raise();
                                break;
                            case FOLD:
                                fold();
                                break;
                        }
                    }
                } catch (Exception e) {
                    Logger.error(e.toString());
                } finally {
                    check.apply(client, table);
                }
            }
        }, DELAY);

        return action;
    }

    private void fold() throws Exception {
        if (table.fold(bot)) {
            client.sendText(String.format("%s has folded", bot.getName()));
        }
    }

    private void raise() throws Exception {
        int raise = table.raise(bot);
        if (raise != -1) {
            client.sendText(String.format("%s(%d) raised by %d, pot %d",
                    bot.getName(),
                    bot.getChips(),
                    raise,
                    table.getPot()));
        }
    }

    private void call() throws Exception {
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

            if (bot.getCall() > 0) {
                table.refund(bot.getCall());
            }
        }
    }

    /**
     * AI for the bot. Input param cmd is Action performed by the player that bet before
     *
     * @param cmd Previous action (action of some other Player)
     * @return Bot's call as the result to @cmd
     */
    private Action action(Action cmd) {
        Strategy s = chooseStrategy();
        if (cmd == Action.DEAL) {
            if (bot.isTurn())
                return s.action(cmd);
            else
                return Action.DEAL; //ignore
        }
        return s.action(cmd);
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
}
