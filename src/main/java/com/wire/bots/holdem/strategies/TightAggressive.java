package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.Hand;
import com.wire.bots.holdem.HandStrength;

public class TightAggressive implements Strategy {
    @Override
    public Action action(Hand hand, int call) {
        if (hand == null)
            return Action.CALL;

        HandStrength strength = hand.getStrength();

        // it was a raise
        if (call > 0) {
            if (strength.ordinal() >= HandStrength.TwoPair.ordinal())
                return Action.RAISE;
            else
                return Action.CALL;
        }

        // it was a call

        if (strength.ordinal() >= HandStrength.ThreeOfKind.ordinal())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
