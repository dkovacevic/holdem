package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.Hand;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;

public class TightAggressive implements Strategy {
    private final Player bot;

    public TightAggressive(Player bot) {
        this.bot = bot;
    }

    @Override
    public Action action(Action cmd) {
        Hand hand = bot.getBestHand();
        if (hand == null)
            return Action.CALL;

        HandStrength strength = hand.getStrength();
        int call = bot.getCall();

        // it was a raise
        if (call > 0) {
            if (strength.ordinal() >= HandStrength.ThreeOfKind.ordinal())
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
