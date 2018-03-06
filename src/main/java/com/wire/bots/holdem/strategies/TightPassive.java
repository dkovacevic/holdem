package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.Hand;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;

public class TightPassive implements Strategy {
    private final Player bot;

    public TightPassive(Player bot) {
        this.bot = bot;
    }

    @Override
    public Action action(Action cmd) {
        Hand hand = bot.getBestHand();
        if (bot.getBoard().isEmpty()) {
            if (cmd == Action.RAISE && hand.strongestCard() < 10)
                return Action.FOLD;
            else
                return Action.CALL;
        }

        HandStrength strength = hand.getStrength();
        int call = bot.getCall();

        // it was a raise
        if (call > 0) {
            if (strength.ordinal() >= HandStrength.OnePair.ordinal())
                return Action.CALL;
            else
                return Action.FOLD;
        }

        // it was a call

        if (strength.ordinal() >= HandStrength.Straight.ordinal())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
