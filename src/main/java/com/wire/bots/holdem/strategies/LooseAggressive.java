package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.Player;

public class LooseAggressive extends BaseStrategy implements Strategy {

    public LooseAggressive(Player bot) {
        super(bot);
    }

    @Override
    public Action action(Action cmd) {
        if (!flop()) {
            if (able() && playable()) // One pair or cards stronger than 10
                return Action.RAISE;
            else
                return Action.CALL;
        }

        // it was a raise
        if (call > 0) {
            if (getChance() > 50f && able())
                return Action.RAISE;
            else
                return Action.CALL;
        }

        // it was a check or a fold

        if (getChance() > 50f && able())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
