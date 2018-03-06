package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;

public class TightAggressive extends BaseStrategy implements Strategy {
    public TightAggressive(Player bot) {
        super(bot);
    }

    @Override
    public Action action(Action cmd) {
        if (!flop()) {
            return Action.CALL;
        }

        // it was a raise
        if (call > 0) {
            if (strength.ordinal() >= HandStrength.ThreeOfKind.ordinal() && able())
                return Action.RAISE;
            else
                return Action.CALL;
        }

        // it was a call

        if (strength.ordinal() >= HandStrength.ThreeOfKind.ordinal() && able())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
