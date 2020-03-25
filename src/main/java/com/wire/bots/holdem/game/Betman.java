package com.wire.bots.holdem.game;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.strategies.*;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Poll;

import java.util.Random;
import java.util.function.Function;

class Betman {
    //private static final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(2);

    private final WireClient client;
    private final Table table;
    private final Player bot;

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

    boolean action(Action cmd, Function<WireClient, Boolean> check) throws Exception {
        boolean ret = false;
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

        check.apply(client);
        return ret;
    }

    private boolean fold() throws Exception {
        if (table.fold(bot)) {
            String msg = String.format("%s has folded", bot.getName());
            sendMessage(msg);
            return true;
        }
        return false;
    }

    private boolean raise() throws Exception {
        int raise = table.raise(bot);
        if (raise != -1) {
            String text = String.format("%s(%d) raised by %d, pot %d",
                    bot.getName(),
                    bot.getChips(),
                    raise,
                    table.getPot());

            for (Player active : table.getActivePlayers()) {
                Poll poll = new Poll();
                poll.addText(text);
                final String call = active.getCall() > 0
                        ? "Call with " + active.getCall()
                        : "Check";
                poll.addButton("call", call);
                poll.addButton("raise", "Raise by " + raise);
                poll.addButton("fold", "Fold");
                client.send(poll, active.getId());
            }
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

            sendMessage(msg);

            int dept = bot.getCall();
            if (dept > 0) {
                table.refund(dept);
            }
            return true;
        }
        return false;
    }

    private void sendMessage(String msg) throws Exception {
        client.sendText(msg);

//        executor.execute(() -> {
//            try {
//                client.sendText(msg);
//            } catch (Exception e) {
//                Logger.error("Betman.sendMessage: %s", e);
//            }
//        });
    }
}
