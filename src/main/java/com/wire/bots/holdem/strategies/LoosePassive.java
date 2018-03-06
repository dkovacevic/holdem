package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;

public class LoosePassive extends BaseStrategy implements Strategy {

    public LoosePassive(Player bot) {
        super(bot);
    }

    @Override
    public Action action(Action cmd) {
        if (!flop())
            return Action.CALL;

        if (call > 0) {
            if (strength.ordinal() >= HandStrength.OnePair.ordinal())
                return Action.CALL;
            else
                return Action.FOLD;
        }

        // it was a call

        if (strength.ordinal() > HandStrength.ThreeOfKind.ordinal() && able())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
