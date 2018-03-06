package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;

public class TightPassive extends BaseStrategy implements Strategy {
    public TightPassive(Player bot) {
        super(bot);
    }

    @Override
    public Action action(Action cmd) {
        if (!flop()) {
            if (cmd == Action.RAISE && hand.strongestCard() < 10)
                return Action.FOLD;
            else
                return Action.CALL;
        }

        // it was a raise
        if (call > 0) {
            if (strength.ordinal() >= HandStrength.OnePair.ordinal())
                return Action.CALL;
            else
                return Action.FOLD;
        }

        // it was a call

        if (strength.ordinal() >= HandStrength.Straight.ordinal() && able())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
