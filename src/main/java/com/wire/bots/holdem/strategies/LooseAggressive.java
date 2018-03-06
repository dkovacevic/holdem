package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;

public class LooseAggressive extends BaseStrategy implements Strategy {

    public LooseAggressive(Player bot) {
        super(bot);
    }

    @Override
    public Action action(Action cmd) {
        if (!flop()) {
            if (able() && (hand.onePair() != -1 || hand.strongestCard() > 7)) // One pair or High card stronger than 10
                return Action.RAISE;
            else
                return Action.CALL;
        }

        // it was a raise
        if (call > 0) {
            if (strength.ordinal() >= HandStrength.TwoPair.ordinal() && able())
                return Action.RAISE;
            else
                return Action.CALL;
        }

        // it was a check or a fold

        if (strength.ordinal() >= HandStrength.OnePair.ordinal() && able())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
