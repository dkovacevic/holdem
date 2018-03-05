package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.Hand;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;

public class LoosePassive implements Strategy {
    private final Player bot;

    public LoosePassive(Player bot) {
        this.bot = bot;
    }

    @Override
    public Action action(Action cmd) {
        Hand hand = bot.getBestHand();
        if (hand == null)
            return Action.CALL;

        HandStrength strength = hand.getStrength();

        // it was a raise
        if (bot.getCall() > 0) {
            if (strength.ordinal() >= HandStrength.OnePair.ordinal())
                return Action.CALL;
            else
                return Action.FOLD;
        }

        // it was a call

        if (strength.ordinal() > HandStrength.ThreeOfKind.ordinal())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
