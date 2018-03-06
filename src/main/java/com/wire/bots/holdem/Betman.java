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
    private Player bot;

    Betman(Player bot) {
        this.bot = bot;
    }

    Action action(WireClient client, Table table, Action cmd, BiFunction<WireClient, Table, Boolean> check) {
        Action action = action(cmd);
        switch (action) {
            case CALL:
                call(client, table, check);
                break;
            case RAISE:
                raise(client, table, check);
                break;
            case FOLD:
                fold(client, table, check);
                break;
        }
        return action;
    }

    private void fold(WireClient client, Table table, BiFunction<WireClient, Table, Boolean> check) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (table.fold(bot)) {
                        client.sendText(String.format("%s has folded", bot.getName()));

                        check.apply(client, table);
                    }
                } catch (Exception e) {
                    Logger.error(e.toString());
                }
            }
        }, DELAY);
    }

    private void raise(WireClient client, Table table, BiFunction<WireClient, Table, Boolean> check) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    int raise = table.raise(bot);
                    if (raise != -1) {
                        client.sendText(String.format("%s(%d) raised by %d, pot %d",
                                bot.getName(),
                                bot.getChips(),
                                raise,
                                table.getPot()));

                        check.apply(client, table);
                    }
                } catch (Exception e) {
                    Logger.error(e.toString());
                }
            }
        }, DELAY);
    }

    private void call(WireClient client, Table table, BiFunction<WireClient, Table, Boolean> check) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    int call = table.call(bot);
                    if (call != -1) {
                        String msg = call == 0
                                ? String.format("%s(%d) checked. pot: %d",
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

                        check.apply(client, table);
                    }
                } catch (Exception e) {
                    Logger.error(e.toString());
                }
            }
        }, DELAY);
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
            // Bot is the Caller
            if (bot.getRole() == Role.Caller)
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
